package fhirspark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.hl7.fhir.r4.model.Observation;
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
import fhirspark.geneticalternations.GeneticAlternationsAdapter;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.ClinicalData;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;

public class JsonFhirMapper {

    private Settings settings;

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client;
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    OncoKbDrug drugResolver = new OncoKbDrug();
    PubmedPublication pubmedResolver = new PubmedPublication();
    GeneticAlternationsAdapter geneticAlterationsAdapter = new GeneticAlternationsAdapter();

    public JsonFhirMapper(Settings settings) {
        this.settings = settings;
        this.client = ctx.newRestfulGenericClient(settings.getFhirDbBase());
    }

    public String toJson(String patientId) throws JsonProcessingException {
        List<Mtb> mtbs = new ArrayList<Mtb>();

        Bundle bPatient = (Bundle) client.search().forResource(Patient.class).where(new TokenClientParam("identifier")
                .exactly().systemAndCode("https://cbioportal.org/patient/", patientId)).prettyPrint().execute();

        Patient fhirPatient = (Patient) bPatient.getEntryFirstRep().getResource();

        if (fhirPatient == null)
            return "{}";

        Bundle bCarePlans = (Bundle) client.search().forResource(CarePlan.class)
                .where(new ReferenceClientParam("subject").hasId(harmonizeId(fhirPatient))).prettyPrint().execute();

        List<BundleEntryComponent> carePlans = bCarePlans.getEntry();

        for (int i = 0; i < carePlans.size(); i++) {
            CarePlan mtbCarePlan = (CarePlan) carePlans.get(i).getResource();
            if (mtbCarePlan.hasPartOf())
                continue; // only use root CarePlan

            Mtb mtb = new Mtb().withId(mtbCarePlan.getIdentifierFirstRep().getValue())
                    .withGeneralRecommendation(mtbCarePlan.getNoteFirstRep().getText())
                    .withSamples(new ArrayList<String>());
            mtbs.add(mtb);

            switch (mtbCarePlan.getStatus().toCode()) {
                case "draft":
                    mtb.setMtbState("Draft");
                    break;
                case "active":
                    mtb.setMtbState("Completed");
                    break;
                case "revoked":
                    mtb.setMtbState("Archived");
                    break;
            }

            
            if (mtbCarePlan.hasAuthor()) {
                Bundle b2 = (Bundle) client.search().forResource(Practitioner.class)
                        .where(new TokenClientParam("_id").exactly()
                                .code(mtbCarePlan.getAuthor().getId()))
                        .prettyPrint().execute();
                Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();

                mtb.setAuthor(author.getIdentifierFirstRep().getValue());
            }

            List<TherapyRecommendation> therapyRecommendations = new ArrayList<TherapyRecommendation>();
            mtb.setTherapyRecommendations(therapyRecommendations);

            for (int j = 0; j < carePlans.size(); j++) {
                CarePlan therapyRecommendationCarePlan = (CarePlan) carePlans.get(j).getResource();
                if (!therapyRecommendationCarePlan.hasPartOf() || !therapyRecommendationCarePlan.getPartOfFirstRep()
                        .getReferenceElement().getIdPart().equals(mtbCarePlan.getIdElement().getIdPart()))
                    continue; // CarePlan is not part of this mtb

                TherapyRecommendation therapyRecommendation = new TherapyRecommendation();
                therapyRecommendations.add(therapyRecommendation);

                if (therapyRecommendationCarePlan.hasAuthor()) {
                    Bundle b2 = (Bundle) client.search().forResource(Practitioner.class)
                            .where(new TokenClientParam("_id").exactly()
                                    .code(therapyRecommendationCarePlan.getAuthor().getId()))
                            .prettyPrint().execute();
                    Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();

                    therapyRecommendation.setAuthor(author.getIdentifierFirstRep().getValue());
                }

                therapyRecommendation.setId(therapyRecommendationCarePlan.getIdentifierFirstRep().getValue());
                List<String> comments = new ArrayList<String>();
                for (Annotation annotation : therapyRecommendationCarePlan.getNote())
                    comments.add(annotation.getText());
                therapyRecommendation.setComment(comments);

                List<Treatment> treatments = new ArrayList<Treatment>();
                for (CarePlanActivityComponent activity : therapyRecommendationCarePlan.getActivity()) {
                    Treatment treatment = new Treatment();
                    CodeableConcept product = (CodeableConcept) activity.getDetail().getProduct();
                    treatment.setName(product.getCodingFirstRep().getDisplay());
                    treatment.setNcitCode(product.getCodingFirstRep().getCode());
                    treatment.setSynonyms(product.getText());
                    treatments.add(treatment);
                }
                therapyRecommendation.setTreatments(treatments);

                Reasoning reasoning = new Reasoning();
                List<GeneticAlteration> geneticAlterations = new ArrayList<GeneticAlteration>();
                reasoning.setGeneticAlterations(geneticAlterations);
                therapyRecommendation.setReasoning(reasoning);

                List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
                for (Reference reference : therapyRecommendationCarePlan.getSupportingInfo()) {
                    if (reference.getReference().startsWith("https://www.ncbi.nlm.nih.gov/pubmed/")) {
                        fhirspark.restmodel.Reference cBioPortalReference = new fhirspark.restmodel.Reference();
                        cBioPortalReference.setName(reference.getDisplay());
                        cBioPortalReference.setPmid(Integer.parseInt(
                                reference.getReference().replace("https://www.ncbi.nlm.nih.gov/pubmed/", "")));
                        references.add(cBioPortalReference);
                    } else if (reference.getResource() != null && reference.getResource() instanceof Observation)  {
                        GeneticAlteration g = new GeneticAlteration();
                        ((Observation) reference.getResource()).getComponent().forEach(variant -> {
                            switch (variant.getCode().getCodingFirstRep().getCode()) {
                                case "48005-3":
                                    g.setAlteration(variant.getValueCodeableConcept().getCodingFirstRep().getCode()
                                            .replaceFirst("p.", ""));
                                    break;
                                case "81252-9":
                                    g.setEntrezGeneId(Integer
                                            .valueOf(variant.getValueCodeableConcept().getCodingFirstRep().getCode()));
                                    break;
                                case "48018-6":
                                    g.setHugoSymbol(variant.getValueCodeableConcept().getCodingFirstRep().getDisplay());
                                    break;
                            }
                        });
                        geneticAlterations.add(g);
                    }
                }
                therapyRecommendation.setReferences(references);
            }

        }

        return this.objectMapper.writeValueAsString(mtbs);

    }

    public void addOrEditTherapyRecommendation(String patientId, String jsonString)
            throws HL7Exception, IOException, LLPException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        List<Mtb> mtbs = this.objectMapper.readValue(jsonString, new TypeReference<List<Mtb>>() {
        });

        Patient fhirPatient = getOrCreatePatient(bundle, patientId);

        mtbs.forEach(mtb -> {

            CarePlan mtbCarePlan = new CarePlan();
            mtbCarePlan.setId(IdType.newRandomUuid());
            mtbCarePlan.setSubject(new Reference(fhirPatient));

            mtbCarePlan
                    .addIdentifier(new Identifier().setSystem("https://cbioportal.org/patient/").setValue(mtb.getId()));

            switch (mtb.getMtbState()) {
                case "Draft":
                    mtbCarePlan.setStatus(CarePlanStatus.DRAFT);
                    break;
                case "Completed":
                    mtbCarePlan.setStatus(CarePlanStatus.ACTIVE);
                    break;
                case "Archived":
                    mtbCarePlan.setStatus(CarePlanStatus.REVOKED);
            }
            mtbCarePlan.setIntent(CarePlanIntent.PLAN);

            mtbCarePlan.setAuthor(new Reference(getOrCreatePractitioner(bundle, mtb.getAuthor())));

            mtbCarePlan.addNote(new Annotation().setText(mtb.getGeneralRecommendation()));

            mtb.getTherapyRecommendations().forEach(therapyRecommendation -> {

                CarePlan therapyRecommendationCarePlan = new CarePlan();
                therapyRecommendationCarePlan.setId(IdType.newRandomUuid());
                therapyRecommendationCarePlan.addPartOf(new Reference(mtbCarePlan));
                therapyRecommendationCarePlan.setSubject(mtbCarePlan.getSubject());
                therapyRecommendationCarePlan.setIntent(mtbCarePlan.getIntent());

                therapyRecommendationCarePlan.setAuthor(new Reference(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor())));

                therapyRecommendationCarePlan.addIdentifier(new Identifier()
                        .setSystem("https://cbioportal.org/patient/").setValue(therapyRecommendation.getId()));

                therapyRecommendationCarePlan.setStatus(mtbCarePlan.getStatus());

                List<Reference> supportingInfo = new ArrayList<Reference>();

                if (therapyRecommendation.getReasoning().getGeneticAlterations() != null) {
                    therapyRecommendation.getReasoning().getGeneticAlterations().forEach(geneticAlteration -> {
                        Resource geneticVariant = geneticAlterationsAdapter.process(geneticAlteration);
                        supportingInfo.add(new Reference(geneticVariant));
                    });
                }

                if (therapyRecommendation.getReasoning().getClinicalData() != null) {
                    therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
                        try {
                            Method m = Class.forName("fhirspark.clinicaldata." + clinical.getAttributeId())
                                    .getMethod("process", ClinicalData.class);
                            Resource clinicalFhir = (Resource) m.invoke(null, clinical);
                            supportingInfo.add(new Reference(clinicalFhir));
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                }

                for (fhirspark.restmodel.Reference reference : therapyRecommendation.getReferences()) {
                    Reference fhirReference = new Reference();
                    fhirReference.setReference("https://www.ncbi.nlm.nih.gov/pubmed/" + reference.getPmid());
                    String title = reference.getName() != null ? reference.getName()
                            : pubmedResolver.resolvePublication(reference.getPmid());
                    fhirReference.setDisplay(title);
                    supportingInfo.add(fhirReference);
                }
                therapyRecommendationCarePlan.setSupportingInfo(supportingInfo);

                for (Treatment treatment : therapyRecommendation.getTreatments()) {
                    CarePlanActivityComponent activity = new CarePlanActivityComponent();
                    CarePlanActivityDetailComponent detail = new CarePlanActivityDetailComponent();

                    detail.setStatus(CarePlanActivityStatus.NOTSTARTED);

                    String ncitCode = treatment.getNcitCode() != null ? treatment.getNcitCode()
                            : drugResolver.resolveDrug(treatment.getName()).getNcitCode();
                    detail.setProduct(new CodeableConcept()
                            .addCoding(
                                    new Coding("http://ncithesaurus-stage.nci.nih.gov", ncitCode, treatment.getName()))
                            .setText(treatment.getSynonyms()));

                    activity.setDetail(detail);
                    therapyRecommendationCarePlan.addActivity(activity);
                }

                List<Annotation> notes = new ArrayList<Annotation>();
                for (String comment : therapyRecommendation.getComment())
                    notes.add(new Annotation().setText(comment));
                therapyRecommendationCarePlan.setNote(notes);

                bundle.addEntry().setFullUrl(therapyRecommendationCarePlan.getIdElement().getValue())
                        .setResource(therapyRecommendationCarePlan).getRequest().setUrl("CarePlan")
                        .setUrl("CarePlan?identifier=https://cbioportal.org/patient/|" + therapyRecommendation.getId())
                        .setIfNoneExist("identifier=https://cbioportal.org/patient/|" + therapyRecommendation.getId())
                        .setMethod(Bundle.HTTPVerb.PUT);

            });

            bundle.addEntry().setFullUrl(mtbCarePlan.getIdElement().getValue()).setResource(mtbCarePlan).getRequest()
                    .setUrl("CarePlan?identifier=https://cbioportal.org/patient/|" + mtb.getId())
                    .setIfNoneExist("identifier=https://cbioportal.org/patient/|" + mtb.getId())
                    .setMethod(Bundle.HTTPVerb.PUT);

        });

        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

        // if (settings.getHl7v2config().get(0).getSendv2()) {

        // HapiContext context = new DefaultHapiContext();
        // Connection connection =
        // context.newClient(settings.getHl7v2config().get(0).getServer(),
        // settings.getHl7v2config().get(0).getPort(), false);

        // ORU_R01 oru = new ORU_R01();
        // oru.initQuickstart("ORU", "R01", "T");

        // PID v2patient = oru.getPATIENT_RESULT().getPATIENT().getPID();
        // v2patient.getPid3_PatientIdentifierList(0).getIDNumber()
        // .setValue(fhirPatient.getIdentifierFirstRep().getValue());

        // Message response =
        // connection.getInitiator().sendAndReceive(oru.getMessage());

        // System.out.println(oru.encode());
        // System.out.println(response.encode());

        // context.close();
        // }

    }

    public void deleteTherapyRecommendation(String patientId, String therapyRecommendationId) {
        assert (therapyRecommendationId.startsWith(patientId));
        client.delete().resourceConditionalByUrl(
                "CarePlan?identifier=https://cbioportal.org/patient/|" + therapyRecommendationId).execute();
    }

    private Patient getOrCreatePatient(Bundle b, String patientId) {

        Patient patient = new Patient();
        patient.setId(IdType.newRandomUuid());
        patient.addIdentifier(new Identifier().setSystem("https://cbioportal.org/patient/").setValue(patientId));
        b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest().setUrl("Patient")
                .setIfNoneExist("identifier=https://cbioportal.org/patient/|" + patientId)
                .setMethod(Bundle.HTTPVerb.POST);

        return patient;
    }

    private Practitioner getOrCreatePractitioner(Bundle b, String credentials) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(IdType.newRandomUuid());
        practitioner.addIdentifier(new Identifier().setSystem("https://cbioportal.org/patient/").setValue(credentials));
        b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                .setUrl("Practitioner").setIfNoneExist("identifier=https://cbioportal.org/patient/|" + credentials)
                .setMethod(Bundle.HTTPVerb.POST);

        return practitioner;

    }

    private String harmonizeId(IAnyResource resource) {
        if (resource.getIdElement().getValue().startsWith("urn:uuid:"))
            return resource.getIdElement().getValue();
        else
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
    }

}