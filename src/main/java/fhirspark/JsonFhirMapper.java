package fhirspark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityDetailComponent;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityStatus;
import org.hl7.fhir.r4.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.r4.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v281.message.ORU_R01;
import ca.uhn.hl7v2.model.v281.segment.PID;
import fhirspark.restmodel.CBioPortalPatient;
import fhirspark.restmodel.ClinicalData;
import fhirspark.restmodel.Modification;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.Recommender;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;

public class JsonFhirMapper {

    private Settings settings;

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client;
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public JsonFhirMapper(Settings settings) {
        this.settings = settings;
        this.client = ctx.newRestfulGenericClient(settings.getFhirDbBase());
    }

    public String toJson(String patientId) throws JsonProcessingException {
        CBioPortalPatient cBioPortalPatient = new CBioPortalPatient();
        List<TherapyRecommendation> therapyRecommendations = new ArrayList<TherapyRecommendation>();

        Bundle bPatient = (Bundle) client.search().forResource(Patient.class).where(new TokenClientParam("identifier")
                .exactly().systemAndCode("https://cbioportal.org/patient/", patientId)).prettyPrint().execute();

        Patient fhirPatient = (Patient) bPatient.getEntryFirstRep().getResource();

        if (fhirPatient == null)
            return "{}";

        Bundle bCarePlans = (Bundle) client.search().forResource(CarePlan.class)
                .where(new ReferenceClientParam("subject").hasId(harmonizeId(fhirPatient))).prettyPrint().execute();

        List<BundleEntryComponent> carePlans = bCarePlans.getEntry();

        if (carePlans.size() > 0) {
            cBioPortalPatient.setTherapyRecommendations(therapyRecommendations);
        }

        for (int i = 0; i < carePlans.size(); i++) {
            CarePlan carePlan = (CarePlan) carePlans.get(i).getResource();
            TherapyRecommendation therapyRecommendation = new TherapyRecommendation();
            therapyRecommendations.add(therapyRecommendation);

            List<Modification> modifications = new ArrayList<Modification>();
            Modification created = new Modification();
            created.setModified("Created");
            created.setTimestamp(carePlan.getCreatedElement().asStringValue());
            Recommender recommender = new Recommender();
            if (carePlan.hasAuthor()) {
                Bundle b2 = (Bundle) client.search().forResource(Practitioner.class)
                        .where(new TokenClientParam("_id").exactly().code(carePlan.getAuthor().getId())).prettyPrint()
                        .execute();
                Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();

                recommender.setCredentials(author.getIdentifierFirstRep().getValue());
            }

            created.setRecommender(recommender);
            modifications.add(created);
            therapyRecommendation.setModifications(modifications);

            therapyRecommendation.setId(carePlan.getIdentifierFirstRep().getValue());
            List<String> comments = new ArrayList<String>();
            for (Annotation annotation : carePlan.getNote())
                comments.add(annotation.getText());
            therapyRecommendation.setComment(comments);

            List<Treatment> treatments = new ArrayList<Treatment>();
            for (CarePlanActivityComponent activity : carePlan.getActivity()) {
                Treatment treatment = new Treatment();
                CodeableConcept product = (CodeableConcept) activity.getDetail().getProduct();
                treatment.setName(product.getCodingFirstRep().getDisplay());
                treatment.setNcitCode(product.getCodingFirstRep().getCode());
                treatment.setSynonyms(product.getText());
                treatments.add(treatment);
            }
            therapyRecommendation.setTreatments(treatments);

            Reasoning reasoning = new Reasoning();
            therapyRecommendation.setReasoning(reasoning);

            List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
            for (Reference reference : carePlan.getSupportingInfo()) {
                if (reference.hasType()) {
                    switch (reference.getType()) {
                        case "Observation":
                            break;

                        default:
                            break;
                    }
                } else {
                    if (reference.getReference().startsWith("https://www.ncbi.nlm.nih.gov/pubmed/")) {
                        fhirspark.restmodel.Reference cBioPortalReference = new fhirspark.restmodel.Reference();
                        cBioPortalReference.setName(reference.getDisplay());
                        cBioPortalReference.setPmid(Integer.parseInt(
                                reference.getReference().replace("https://www.ncbi.nlm.nih.gov/pubmed/", "")));
                        references.add(cBioPortalReference);
                    }
                }
            }
            therapyRecommendation.setReferences(references);
        }

        return this.objectMapper.writeValueAsString(cBioPortalPatient);
    }

    public void addTherapyRecommendation(String id, String jsonString) throws HL7Exception, IOException, LLPException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        TherapyRecommendation therapyRecommendation = this.objectMapper.readValue(jsonString,
                TherapyRecommendation.class);

        Patient fhirPatient = getOrCreatePatient(bundle, id);

        CarePlan carePlan = new CarePlan();
        carePlan.setId(IdType.newRandomUuid());
        carePlan.setSubject(new Reference(harmonizeId(fhirPatient)));

        carePlan.addIdentifier(
                new Identifier().setSystem("https://cbioportal.org/patient/").setValue(therapyRecommendation.getId()));

        carePlan.setStatus(CarePlanStatus.DRAFT);
        carePlan.setIntent(CarePlanIntent.PLAN);

        therapyRecommendation.getModifications().forEach((mod) -> {
            if (mod.getModified().equals("Created")) {
                DateTimeType created = new DateTimeType();
                created.setValueAsString(mod.getTimestamp());
                carePlan.setCreatedElement(created);
                Practitioner author = getOrCreatePractitioner(bundle, mod.getRecommender().getCredentials());
                carePlan.setAuthor(new Reference(harmonizeId(author)));
            }
        });

        List<Reference> supportingInfo = new ArrayList<Reference>();
        therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
            try {
                Method m = Class.forName("fhirspark.clinicaldata." + clinical.getAttributeId()).getMethod("process",
                        ClinicalData.class);
                Resource clinicalFhir = (Resource) m.invoke(null, clinical);
                bundle.addEntry().setFullUrl(clinicalFhir.getIdElement().getValue()).setResource(clinicalFhir).getRequest()
                .setUrl(clinicalFhir.getIdElement().getResourceType()).setMethod(Bundle.HTTPVerb.POST);
                supportingInfo.add(new Reference(clinicalFhir));
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        for (fhirspark.restmodel.Reference reference : therapyRecommendation.getReferences()) {
            Reference fhirReference = new Reference();
            fhirReference.setReference("https://www.ncbi.nlm.nih.gov/pubmed/" + reference.getPmid());
            fhirReference.setDisplay(reference.getName());
            supportingInfo.add(fhirReference);
        }
        carePlan.setSupportingInfo(supportingInfo);

        for (Treatment treatment : therapyRecommendation.getTreatments()) {
            CarePlanActivityComponent activity = new CarePlanActivityComponent();
            CarePlanActivityDetailComponent detail = new CarePlanActivityDetailComponent();

            detail.setStatus(CarePlanActivityStatus.NOTSTARTED);

            detail.setProduct(
                    new CodeableConcept().addCoding(new Coding("http://ncithesaurus-stage.nci.nih.gov", treatment.getNcitCode(), treatment.getName()))
                            .setText(treatment.getSynonyms()));

            activity.setDetail(detail);
            carePlan.addActivity(activity);
        }

        List<Annotation> notes = new ArrayList<Annotation>();
        for (String comment : therapyRecommendation.getComment())
            notes.add(new Annotation().setText(comment));
        carePlan.setNote(notes);

        bundle.addEntry().setFullUrl(carePlan.getIdElement().getValue()).setResource(carePlan).getRequest()
                .setUrl("CarePlan").setMethod(Bundle.HTTPVerb.POST);

        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

        if (settings.getHl7v2config().get(0).getSendv2()) {

            HapiContext context = new DefaultHapiContext();
            Connection connection = context.newClient(settings.getHl7v2config().get(0).getServer(),
                    settings.getHl7v2config().get(0).getPort(), false);

            ORU_R01 oru = new ORU_R01();
            oru.initQuickstart("ORU", "R01", "T");

            PID v2patient = oru.getPATIENT_RESULT().getPATIENT().getPID();
            v2patient.getPid3_PatientIdentifierList(0).getIDNumber()
                    .setValue(fhirPatient.getIdentifierFirstRep().getValue());

            Message response = connection.getInitiator().sendAndReceive(oru.getMessage());

            System.out.println(oru.encode());
            System.out.println(response.encode());

            context.close();
        }

    }

    public void editTherapyRecommendation(String params, String params2, String body) {
    }

    public void deleteTherapyRecommendation(String patientId, String therapyRecommendationId) {
        assert(therapyRecommendationId.startsWith(patientId));
        client.delete().resourceConditionalByUrl("CarePlan?identifier=https://cbioportal.org/patient/|" + therapyRecommendationId).execute();
    }

    public void editGeneticCounselingRecommendation(String params, String body) {
    }

    public void editRebiopsyRecommendation(String params, String body) {
    }

    public void editComment(String params, String body) {
    }

    private Patient getOrCreatePatient(Bundle b, String patientId) {

        Bundle b2 = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode("https://cbioportal.org/patient/", patientId))
                .prettyPrint().execute();

        Patient p = (Patient) b2.getEntryFirstRep().getResource();

        if (p != null && p.getIdentifierFirstRep().hasValue()) {
            return p;
        } else {

            Patient patient = new Patient();
            patient.setId(IdType.newRandomUuid());
            patient.addIdentifier(new Identifier().setSystem("https://cbioportal.org/patient/").setValue(patientId));
            b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest()
                    .setUrl("Patient").setMethod(Bundle.HTTPVerb.POST);

            return patient;
        }

    }

    private Practitioner getOrCreatePractitioner(Bundle b, String credentials) {

        Bundle b2 = (Bundle) client.search().forResource(Practitioner.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode("https://cbioportal.org/patient/", credentials))
                .prettyPrint().execute();

        Practitioner p = (Practitioner) b2.getEntryFirstRep().getResource();

        if (p != null && p.getIdentifierFirstRep().hasValue()) {
            return p;
        } else {

            Practitioner practitioner = new Practitioner();
            practitioner.setId(IdType.newRandomUuid());
            practitioner.addIdentifier(new Identifier().setSystem("https://cbioportal.org/patient/").setValue(credentials));
            b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                    .setUrl("Practitioner").setMethod(Bundle.HTTPVerb.POST);

            return practitioner;
        }

    }

    private String harmonizeId(IAnyResource resource) {
        if(resource.getIdElement().getValue().startsWith("urn:uuid:"))
            return resource.getIdElement().getValue();
        else
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
    }

}