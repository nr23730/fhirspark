package fhirspark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;

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
import fhirspark.adapter.DrugAdapter;
import fhirspark.adapter.GeneticAlternationsAdapter;
import fhirspark.adapter.SpecimenAdapter;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.CbioportalRest;
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

    SpecimenAdapter specimenAdapter = new SpecimenAdapter();

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

        Bundle bDiagnosticReports = (Bundle) client.search().forResource(DiagnosticReport.class)
                .where(new ReferenceClientParam("subject").hasId(harmonizeId(fhirPatient))).prettyPrint().execute();

        List<BundleEntryComponent> diagnosticReports = bDiagnosticReports.getEntry();

        for (int i = 0; i < diagnosticReports.size(); i++) {
            DiagnosticReport diagnosticReport = (DiagnosticReport) diagnosticReports.get(i).getResource();

            Mtb mtb = new Mtb().withTherapyRecommendations(new ArrayList<TherapyRecommendation>())
                    .withSamples(new ArrayList<String>());
            for (Mtb mtbCandidate : mtbs) {
                mtb = mtbCandidate.getId().equals(diagnosticReport.getIdentifier().get(0).getValue()) ? mtbCandidate
                        : new Mtb().withTherapyRecommendations(new ArrayList<TherapyRecommendation>())
                                .withSamples(new ArrayList<String>());
            }

            mtb.withId(diagnosticReport.getIdentifier().get(0).getValue());
            mtb.withSamples(new ArrayList<String>());
            if (!mtbs.contains(mtb))
                mtbs.add(mtb);

            if (diagnosticReport.hasStatus()) {
                switch (diagnosticReport.getStatus().toCode()) {
                    case "partial":
                        mtb.setMtbState("DRAFT");
                        break;
                    case "final":
                        mtb.setMtbState("COMPLETED");
                        break;
                    case "cancelled":
                        mtb.setMtbState("ARCHIVED");
                        break;
                }
            }

            for (Reference specimen : diagnosticReport.getSpecimen())
                mtb.getSamples().add(((Specimen) specimen.getResource()).getIdentifierFirstRep().getValue());

            mtb.setGeneralRecommendation(diagnosticReport.getConclusion());

            if (diagnosticReport.hasPerformer()) {
                Bundle b2 = (Bundle) client.search().forResource(Practitioner.class).where(
                        new TokenClientParam("_id").exactly().code(diagnosticReport.getPerformerFirstRep().getId()))
                        .prettyPrint().execute();
                Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();

                mtb.setAuthor(author.getIdentifierFirstRep().getValue());
            }

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            mtb.setDate(f.format(diagnosticReport.getEffectiveDateTimeType().toCalendar().getTime()));

            TherapyRecommendation therapyRecommendation = new TherapyRecommendation();
            mtb.getTherapyRecommendations().add(therapyRecommendation);

            therapyRecommendation.setAuthor(mtb.getAuthor());

            therapyRecommendation.setId(diagnosticReport.getIdentifier().get(1).getValue());

            List<String> comments = new ArrayList<String>();
            // for (Annotation annotation : therapyRecommendationCarePlan.getNote())
            // comments.add(annotation.getText());
            therapyRecommendation.setComment(comments);

            List<Treatment> treatments = new ArrayList<Treatment>();
            List<Extension> recommendedActionReferences = diagnosticReport.getExtensionsByUrl(
                    "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction");
            for (Extension recommendedActionReference : recommendedActionReferences) {

                Task t = (Task) ((Reference) recommendedActionReference.getValue()).getResource();
                if (t != null) {
                    assert (t.getMeta().getProfile().get(0).getValue()
                            .equals("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup"));
                    Coding c = t.getCode().getCodingFirstRep();
                    switch (c.getCode()) {
                        case "LA14021-2":
                            mtb.setRebiopsyRecommendation(true);
                            break;
                        case "LA14020-4":
                            mtb.setGeneticCounselingRecommendation(true);
                            break;
                    }
                } else {
                    Bundle bRecommendedAction = (Bundle) client.search().forResource(Task.class)
                            .where(new TokenClientParam("_id").exactly()
                                    .code(((Reference) recommendedActionReference.getValue()).getReference()))
                            .prettyPrint().execute();
                    MedicationStatement medicationStatement = (MedicationStatement) ((Task) bRecommendedAction.getEntryFirstRep().getResource())
                            .getFocus().getResource();
                    Coding drug = medicationStatement.getMedicationCodeableConcept().getCodingFirstRep();
                    treatments.add(new Treatment().withNcitCode(drug.getCode()).withName(drug.getDisplay()));

                    therapyRecommendation.setComment(new ArrayList<String>());
                    for(Annotation a : medicationStatement.getNote())
                        therapyRecommendation.getComment().add(a.getText());
                    
                }
            }
            therapyRecommendation.setTreatments(treatments);

            List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
            for (Extension relatedArtifact : diagnosticReport.getExtensionsByUrl(
                    "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact"))
                references.add(new fhirspark.restmodel.Reference()
                        .withPmid(Integer.valueOf(((RelatedArtifact) relatedArtifact.getValue()).getUrl()
                                .replaceFirst("https://www.ncbi.nlm.nih.gov/pubmed/", "")))
                        .withName((((RelatedArtifact) relatedArtifact.getValue()).getCitation())));

            Reasoning reasoning = new Reasoning();
            List<GeneticAlteration> geneticAlterations = new ArrayList<GeneticAlteration>();
            reasoning.setGeneticAlterations(geneticAlterations);
            therapyRecommendation.setReasoning(reasoning);

            for (Reference reference : diagnosticReport.getResult()) {
                GeneticAlteration g = new GeneticAlteration();
                ((Observation) reference.getResource()).getComponent().forEach(variant -> {
                    switch (variant.getCode().getCodingFirstRep().getCode()) {
                        case "48005-3":
                            g.setAlteration(variant.getValueCodeableConcept().getCodingFirstRep().getCode()
                                    .replaceFirst("p.", ""));
                            break;
                        case "81252-9":
                            g.setEntrezGeneId(
                                    Integer.valueOf(variant.getValueCodeableConcept().getCodingFirstRep().getCode()));
                            break;
                        case "48018-6":
                            g.setHugoSymbol(variant.getValueCodeableConcept().getCodingFirstRep().getDisplay());
                            break;
                    }
                });
                geneticAlterations.add(g);
            }

            therapyRecommendation.setReferences(references);

        }

        return this.objectMapper.writeValueAsString(new CbioportalRest().withId(patientId).withMtbs(mtbs));

    }

    public void addOrEditTherapyRecommendation(String patientId, String jsonString)
            throws HL7Exception, IOException, LLPException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        List<Mtb> mtbs = this.objectMapper.readValue(jsonString, CbioportalRest.class).getMtbs();

        Patient fhirPatient = getOrCreatePatient(bundle, patientId);

        mtbs.forEach(mtb -> {

            mtb.getTherapyRecommendations().forEach(therapyRecommendation -> {

                DiagnosticReport diagnosticReport = new DiagnosticReport();
                diagnosticReport.setId(IdType.newRandomUuid());

                diagnosticReport.setSubject(new Reference(fhirPatient));

                diagnosticReport.getCode().addCoding(
                        new Coding("http://loing.org", "81247-9", "Master HL7 genetic variant reporting panel"));

                diagnosticReport.getIdentifier()
                        .add(new Identifier().setSystem("https://cbioportal.org/mtb/").setValue(mtb.getId()));
                diagnosticReport.getIdentifier()
                        .add(new Identifier().setSystem("https://cbioportal.org/therapyrecommendation/")
                                .setValue(therapyRecommendation.getId()));

                diagnosticReport.setConclusion(mtb.getGeneralRecommendation());

                diagnosticReport.getEffectiveDateTimeType().fromStringValue(mtb.getDate());

                diagnosticReport.addPerformer(
                        new Reference(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor())));

                if (mtb.getMtbState() != null) {
                    switch (mtb.getMtbState().toUpperCase()) {
                        case "DRAFT":
                            diagnosticReport.setStatus(DiagnosticReportStatus.PARTIAL);
                            break;
                        case "COMPLETED":
                            diagnosticReport.setStatus(DiagnosticReportStatus.FINAL);
                            break;
                        case "ARCHIVED":
                            diagnosticReport.setStatus(DiagnosticReportStatus.CANCELLED);
                    }
                } else {
                    diagnosticReport.setStatus(DiagnosticReportStatus.PARTIAL);
                }

                mtb.getSamples().forEach(sample -> diagnosticReport
                        .addSpecimen(new Reference(specimenAdapter.process(new Reference(fhirPatient), sample))));

                if (therapyRecommendation.getReasoning().getGeneticAlterations() != null) {
                    therapyRecommendation.getReasoning().getGeneticAlterations().forEach(geneticAlteration -> {
                        Resource geneticVariant = geneticAlterationsAdapter.process(geneticAlteration);
                        diagnosticReport.addResult(new Reference(geneticVariant));
                    });
                }

                if (mtb.getGeneticCounselingRecommendation() != null && mtb.getGeneticCounselingRecommendation()) {
                    Task t = new Task();
                    t.getMeta().addProfile(
                            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup");
                    t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                    t.getCode().setText("Recommended follow-up")
                            .addCoding(new Coding("http://loinc.org", "LA14020-4", "Genetic counseling recommended"));

                    Extension ex = new Extension()
                            .setUrl("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction");
                    ex.setValue(new Reference(t));
                    diagnosticReport.addExtension(ex);
                }

                if (mtb.getRebiopsyRecommendation() != null && mtb.getRebiopsyRecommendation()) {
                    Task t = new Task();
                    t.getMeta().addProfile(
                            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup");
                    t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                    t.getCode().setText("Recommended follow-up")
                            .addCoding(new Coding("http://loinc.org", "LA14021-2", "Confirmatory testing recommended"));

                    Extension ex = new Extension()
                            .setUrl("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction");
                    ex.setValue(new Reference(t));
                    diagnosticReport.addExtension(ex);
                }

                if (therapyRecommendation.getReasoning().getClinicalData() != null) {
                    therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
                        try {
                            Method m = Class.forName("fhirspark.clinicaldata." + clinical.getAttributeId())
                                    .getMethod("process", ClinicalData.class);
                            Resource clinicalFhir = (Resource) m.invoke(null, clinical);
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
                    String title = reference.getName() != null ? reference.getName()
                            : pubmedResolver.resolvePublication(reference.getPmid());
                    Extension ex = new Extension()
                            .setUrl("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact");
                    RelatedArtifact relatedArtifact = new RelatedArtifact().setType(RelatedArtifactType.CITATION)
                            .setUrl("https://www.ncbi.nlm.nih.gov/pubmed/" + reference.getPmid()).setCitation(title);
                    ex.setValue(relatedArtifact);
                    diagnosticReport.addExtension(ex);
                }

                for (Treatment treatment : therapyRecommendation.getTreatments()) {
                    DrugAdapter drugAdapter = new DrugAdapter();
                    Task medicationChange = new Task().setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL)
                            .setFor(new Reference(fhirPatient));
                    medicationChange.setId(IdType.newRandomUuid());
                    medicationChange.getMeta()
                            .addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-med-chg");

                    MedicationStatement ms = drugAdapter.process(new Reference(fhirPatient), treatment);

                    medicationChange.getCode()
                            .addCoding(new Coding("http://loinc.org", "LA26421-0", "Consider alternative medication"));
                    medicationChange.setFocus(new Reference(ms));
                    String ncit = ms.getMedicationCodeableConcept().getCodingFirstRep().getCode();
                    medicationChange.addIdentifier(
                            new Identifier().setSystem("http://ncithesaurus-stage.nci.nih.gov").setValue(ncit));

                    for(String comment : therapyRecommendation.getComment())
                        ms.getNote().add(new Annotation().setText(comment));

                    Extension ex = new Extension()
                            .setUrl("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction");
                    ex.setValue(new Reference(medicationChange));
                    diagnosticReport.addExtension(ex);

                    bundle.addEntry().setFullUrl(medicationChange.getIdElement().getValue())
                            .setResource(medicationChange).getRequest()
                            .setUrl("Task?identifier=http://ncithesaurus-stage.nci.nih.gov|" + ncit + "&subject="
                                    + fhirPatient.getId())
                            .setIfNoneExist("identifier=Task?identifier=http://ncithesaurus-stage.nci.nih.gov|" + ncit
                                    + "&subject=" + fhirPatient.getId())
                            .setMethod(Bundle.HTTPVerb.PUT);
                }

                bundle.addEntry().setFullUrl(diagnosticReport.getIdElement().getValue()).setResource(diagnosticReport)
                        .getRequest()
                        .setUrl("DiagnosticReport?identifier=https://cbioportal.org/therapyrecommendation/|"
                                + therapyRecommendation.getId())
                        .setIfNoneExist("identifier=https://cbioportal.org/therapyrecommendation/|"
                                + therapyRecommendation.getId())
                        .setMethod(Bundle.HTTPVerb.PUT);

            });

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