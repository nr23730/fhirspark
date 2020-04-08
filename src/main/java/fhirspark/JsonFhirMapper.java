package fhirspark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.model.v281.message.ADT_A01;
import ca.uhn.hl7v2.model.v281.message.ORU_R01;
import ca.uhn.hl7v2.model.v281.segment.MSH;
import ca.uhn.hl7v2.model.v281.segment.PID;
import ca.uhn.hl7v2.model.v281.segment.PTH;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import fhirspark.restmodel.Modification;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.Therapy;

public class JsonFhirMapper {

    private Settings settings;

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client;
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public JsonFhirMapper(Settings settings) {
        this.settings = settings;
        this.client = ctx.newRestfulGenericClient(settings.getFhirDbBase());
    }

    public void fromJson(String jsonString) throws HL7Exception, IOException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        Therapy therapy = this.objectMapper.readValue(jsonString, Therapy.class);

        Patient patient = getOrCreatePatient(bundle, therapy.getId());

        for (TherapyRecommendation therapyRecommendation : therapy.getTherapyRecommendations()) {
            CarePlan carePlan = new CarePlan();
            carePlan.setSubject(new Reference(patient));

            carePlan.addIdentifier(
                    new Identifier().setSystem("cbioportal").setValue(therapyRecommendation.getId()));
            List<Annotation> notes = new ArrayList<Annotation>();
            for (String comment : therapyRecommendation.getComment())
                notes.add(new Annotation().setText(comment));
            carePlan.setNote(notes);

            bundle.addEntry().setFullUrl(carePlan.getIdElement().getValue()).setResource(carePlan).getRequest()
                    .setUrl("CarePlan").setMethod(Bundle.HTTPVerb.POST);
        }

        //Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

        if(settings.getHl7v2config().get(0).getSendv2()) {

            HapiContext context = new DefaultHapiContext();
            //Connection connection = context.newClient(settings.getHl7v2config().get(0).getServer(), settings.getHl7v2config().get(0).getPort(), false);

            ORU_R01 oru = new ORU_R01();
            oru.initQuickstart("ORU", "R01", "T");

            PID v2patient = oru.getPATIENT_RESULT().getPATIENT().getPID();
            v2patient.getPid3_PatientIdentifierList(0).getIDNumber().setValue(patient.getIdentifierFirstRep().getValue());

            System.out.println(oru.encode());
        }

    }

    public String toJson(String patientId) throws JsonProcessingException {
        Therapy therapy = new Therapy();
        List<TherapyRecommendation> therapyRecommendations = new ArrayList<TherapyRecommendation>();

        Bundle bPatient = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode("cbioportal", patientId))
                .prettyPrint().execute();

        Patient patient = (Patient) bPatient.getEntryFirstRep().getResource();

        if (patient == null)
            return "{}";

        Bundle bCarePlans = (Bundle) client.search().forResource(CarePlan.class)
                .where(new ReferenceClientParam("subject").hasId(patient.getIdElement())).prettyPrint().execute();

        List<BundleEntryComponent> carePlans = bCarePlans.getEntry();

        if(carePlans.size() > 0) {
            therapy.setId(patientId);
            therapy.setTherapyRecommendations(therapyRecommendations);
        }

        for (int i = 0; i < carePlans.size(); i++) {
            CarePlan carePlan = (CarePlan) carePlans.get(i).getResource();
            TherapyRecommendation therapyRecommendation = new TherapyRecommendation();
            therapyRecommendations.add(therapyRecommendation);
            List<Modification> modifications = new ArrayList<Modification>();
            therapyRecommendation.setModifications(modifications);

            therapyRecommendation.setId(carePlan.getIdentifierFirstRep().getValue());
            List<String> comments = new ArrayList<String>();
            for (Annotation annotation : carePlan.getNote())
                comments.add(annotation.getText());
            therapyRecommendation.setComment(comments);

            List<Treatment> treatments = new ArrayList<Treatment>();
            therapyRecommendation.setTreatments(treatments);

            Reasoning reasoning = new Reasoning();
            therapyRecommendation.setReasoning(reasoning);

            List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
            therapyRecommendation.setReferences(references);
        }

        return this.objectMapper.writeValueAsString(therapy);
    }

    private Patient getOrCreatePatient(Bundle b, String patientId) {

        Bundle b2 = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode("cbioportal", patientId))
                .prettyPrint().execute();

        Patient p = (Patient) b2.getEntryFirstRep().getResource();

        if (p != null && p.getIdentifierFirstRep().hasValue()) {
            return p;
        } else {

            Patient patient = new Patient();
            patient.setId(IdType.newRandomUuid());
            patient.addIdentifier(new Identifier().setSystem("cbioportal").setValue(patientId));
            b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest().setMethod(Bundle.HTTPVerb.POST);

            return patient;
        }

    }

}