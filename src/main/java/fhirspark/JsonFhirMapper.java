package fhirspark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import org.hl7.fhir.r4.model.Evidence;
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
import ca.uhn.hl7v2.model.v281.datatype.CWE;
import ca.uhn.hl7v2.model.v281.datatype.ST;
import ca.uhn.hl7v2.model.v281.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v281.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v281.message.ORU_R01;
import ca.uhn.hl7v2.model.v281.segment.NTE;
import ca.uhn.hl7v2.model.v281.segment.OBR;
import ca.uhn.hl7v2.model.v281.segment.OBX;
import ca.uhn.hl7v2.model.v281.segment.SPM;
import fhirspark.adapter.DrugAdapter;
import fhirspark.adapter.EvidenceAdapter;
import fhirspark.adapter.GeneticAlternationsAdapter;
import fhirspark.adapter.SpecimenAdapter;
import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.resolver.PubmedPublication;
import fhirspark.resolver.model.genenames.Doc;
import fhirspark.restmodel.CbioportalRest;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;

public class JsonFhirMapper {

    private Settings settings;
    private static String LOINC_URI = "http://loinc.org";
    private static String PATIENT_URI = "https://cbioportal.org/patient/";
    private static String MTB_URI = "https://cbioportal.org/mtb/";
    private static String RECOMMENDEDACTION_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction";
    private static String FOLLOWUP_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup";
    private static String RELATEDARTIFACT_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact";
    private static String MEDICATIONCHANGE_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-med-chg";
    private static String PUBMED_URI = "https://www.ncbi.nlm.nih.gov/pubmed/";
    private static String NCIT_URI = "http://ncithesaurus-stage.nci.nih.gov";
    private static String THERAPYRECOMMENDATION_URI = "https://cbioportal.org/therapyrecommendation/";
    private static String GENOMICSREPORT_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/genomics-report";
    private static String GENOMIC_URI = "http://terminology.hl7.org/CodeSystem/v2-0074";

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client;
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    OncoKbDrug drugResolver = new OncoKbDrug();
    PubmedPublication pubmedResolver = new PubmedPublication();

    GeneticAlternationsAdapter geneticAlterationsAdapter = new GeneticAlternationsAdapter();
    DrugAdapter drugAdapter = new DrugAdapter();
    SpecimenAdapter specimenAdapter = new SpecimenAdapter();
    EvidenceAdapter evidenceAdapter = new EvidenceAdapter();

    public JsonFhirMapper(Settings settings) {
        this.settings = settings;
        this.client = ctx.newRestfulGenericClient(settings.getFhirDbBase());
    }

    public String toJson(String patientId) throws JsonProcessingException {
        List<Mtb> mtbs = new ArrayList<Mtb>();
        Bundle bPatient = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(PATIENT_URI, patientId)).prettyPrint()
                .execute();
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
            if (!mtbs.contains(mtb))
                mtbs.add(mtb);

            if (diagnosticReport.hasPerformer()) {
                Bundle b2 = (Bundle) client.search().forResource(Practitioner.class).where(
                        new TokenClientParam("_id").exactly().code(diagnosticReport.getPerformerFirstRep().getId()))
                        .prettyPrint().execute();
                Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();
                mtb.setAuthor(author.getIdentifierFirstRep().getValue());
            }

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            mtb.setDate(f.format(diagnosticReport.getEffectiveDateTimeType().toCalendar().getTime()));

            mtb.setGeneralRecommendation(diagnosticReport.getConclusion());

            // GENETIC COUNSELING HERE

            mtb.setId(diagnosticReport.getIdentifier().get(0).getValue());

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

            // REBIOPSY HERE

            for (Reference specimen : diagnosticReport.getSpecimen())
                mtb.getSamples().add(((Specimen) specimen.getResource()).getIdentifierFirstRep().getValue());

            TherapyRecommendation therapyRecommendation = new TherapyRecommendation()
                    .withComment(new ArrayList<String>()).withReasoning(new Reasoning());
            mtb.getTherapyRecommendations().add(therapyRecommendation);
            List<ClinicalDatum> clinicalData = new ArrayList<ClinicalDatum>();
            List<GeneticAlteration> geneticAlterations = new ArrayList<GeneticAlteration>();
            therapyRecommendation.getReasoning().withClinicalData(clinicalData)
                    .withGeneticAlterations(geneticAlterations);

            therapyRecommendation.setAuthor(mtb.getAuthor());

            // COMMENTS GET WITHIN MEDICATION

            for (Extension relatedArtifact : diagnosticReport.getExtensionsByUrl(RELATEDARTIFACT_URI)) {
                if (((RelatedArtifact) relatedArtifact.getValue()).getType() == RelatedArtifactType.JUSTIFICATION) {
                    Bundle bEvidence = (Bundle) client.search().forResource(Evidence.class)
                            .where(new TokenClientParam("_id").exactly()
                                    .code(((RelatedArtifact) relatedArtifact.getValue()).getResource()))
                            .prettyPrint().execute();
                    Evidence evidence = (Evidence) bEvidence.getEntryFirstRep().getResource();
                    therapyRecommendation.setEvidenceLevel(evidence.getName());
                }
            }

            therapyRecommendation.setId(diagnosticReport.getIdentifier().get(1).getValue());

            // PUT CLINICAL DATA HERE

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

            List<Treatment> treatments = new ArrayList<Treatment>();
            therapyRecommendation.setTreatments(treatments);
            List<Extension> recommendedActionReferences = diagnosticReport.getExtensionsByUrl(RECOMMENDEDACTION_URI);
            for (Extension recommendedActionReference : recommendedActionReferences) {

                Task t = (Task) ((Reference) recommendedActionReference.getValue()).getResource();
                if (t != null) {
                    assert (t.getMeta().getProfile().get(0).getValue().equals(FOLLOWUP_URI));
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
                    MedicationStatement medicationStatement = (MedicationStatement) ((Task) bRecommendedAction
                            .getEntryFirstRep().getResource()).getFocus().getResource();
                    Coding drug = medicationStatement.getMedicationCodeableConcept().getCodingFirstRep();
                    treatments.add(new Treatment().withNcitCode(drug.getCode()).withName(drug.getDisplay()));

                    for (Annotation a : medicationStatement.getNote())
                        therapyRecommendation.getComment().add(a.getText());
                }
            }

            List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
            for (Extension relatedArtifact : diagnosticReport.getExtensionsByUrl(RELATEDARTIFACT_URI)) {
                if (((RelatedArtifact) relatedArtifact.getValue()).getType() == RelatedArtifactType.CITATION)
                    references.add(new fhirspark.restmodel.Reference()
                            .withPmid(Integer.valueOf(((RelatedArtifact) relatedArtifact.getValue()).getUrl()
                                    .replaceFirst(PUBMED_URI, "")))
                            .withName((((RelatedArtifact) relatedArtifact.getValue()).getCitation())));
            }

            therapyRecommendation.setReferences(references);

        }

        return this.objectMapper.writeValueAsString(new CbioportalRest().withId(patientId).withMtbs(mtbs));

    }

    public void addOrEditMtb(String patientId, String jsonString) throws JsonMappingException, JsonProcessingException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        List<Mtb> mtbs = this.objectMapper.readValue(jsonString, CbioportalRest.class).getMtbs();

        Reference fhirPatient = getOrCreatePatient(bundle, patientId);

        mtbs.forEach(mtb -> {

            mtb.getTherapyRecommendations().forEach(therapyRecommendation -> {

                DiagnosticReport diagnosticReport = new DiagnosticReport();
                diagnosticReport.getMeta().addProfile(GENOMICSREPORT_URI);
                diagnosticReport.setId(IdType.newRandomUuid());
                diagnosticReport.setSubject(fhirPatient);
                diagnosticReport.addCategory().addCoding(new Coding().setSystem(GENOMIC_URI).setCode("GE"));
                diagnosticReport.getCode()
                        .addCoding(new Coding(LOINC_URI, "81247-9", "Master HL7 genetic variant reporting panel"));

                // MTB SECTION

                diagnosticReport.addPerformer(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor()));

                diagnosticReport.getEffectiveDateTimeType().fromStringValue(mtb.getDate());

                diagnosticReport.setConclusion(mtb.getGeneralRecommendation());

                if (mtb.getGeneticCounselingRecommendation() != null && mtb.getGeneticCounselingRecommendation()) {
                    Task t = new Task();
                    t.getMeta().addProfile(FOLLOWUP_URI);
                    t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                    t.getCode().setText("Recommended follow-up")
                            .addCoding(new Coding(LOINC_URI, "LA14020-4", "Genetic counseling recommended"));
                    Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                    ex.setValue(new Reference(t));
                    diagnosticReport.addExtension(ex);
                }

                diagnosticReport.getIdentifier().add(new Identifier().setSystem(MTB_URI).setValue(mtb.getId()));

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

                if (mtb.getRebiopsyRecommendation() != null && mtb.getRebiopsyRecommendation()) {
                    Task t = new Task();
                    t.getMeta().addProfile(FOLLOWUP_URI);
                    t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                    t.getCode().setText("Recommended follow-up")
                            .addCoding(new Coding(LOINC_URI, "LA14021-2", "Confirmatory testing recommended"));
                    Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                    ex.setValue(new Reference(t));
                    diagnosticReport.addExtension(ex);
                }

                mtb.getSamples().forEach(sample -> diagnosticReport
                        .addSpecimen(new Reference(specimenAdapter.process(fhirPatient, sample))));

                // THERAPYRECOMMENDATION SECTION

                // AUTHOR ALREADY SET BY MTB

                // COMMENTS SET WITH MEDICATION

                Evidence evidence = evidenceAdapter.process(therapyRecommendation.getEvidenceLevel());
                evidence.setId(IdType.newRandomUuid());
                bundle.addEntry().setFullUrl(evidence.getIdElement().getValue()).setResource(evidence).getRequest()
                        .setUrl("Evidence")
                        .setIfNoneExist("identifier=Evidence?identifier=" + evidence.getIdentifierFirstRep().getSystem()
                                + "|" + evidence.getIdentifierFirstRep().getValue())
                        .setMethod(Bundle.HTTPVerb.POST);

                Extension evidenceExtension = new Extension().setUrl(RELATEDARTIFACT_URI);
                RelatedArtifact evidenceArtifact = new RelatedArtifact().setType(RelatedArtifactType.JUSTIFICATION)
                        .setResource(harmonizeId(evidence));
                evidenceExtension.setValue(evidenceArtifact);
                diagnosticReport.addExtension(evidenceExtension);

                diagnosticReport.getIdentifier().add(
                        new Identifier().setSystem(THERAPYRECOMMENDATION_URI).setValue(therapyRecommendation.getId()));

                if (therapyRecommendation.getReasoning().getClinicalData() != null) {
                    therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
                        try {
                            Method m = Class.forName("fhirspark.adapter.clinicaldata." + clinical.getAttributeId())
                                    .getMethod("process", ClinicalDatum.class);
                            Resource clinicalFhir = (Resource) m.invoke(null, clinical);
                            diagnosticReport.addResult(new Reference(clinicalFhir));
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

                if (therapyRecommendation.getReasoning().getGeneticAlterations() != null) {
                    therapyRecommendation.getReasoning().getGeneticAlterations().forEach(geneticAlteration -> {
                        Resource geneticVariant = geneticAlterationsAdapter.process(geneticAlteration);
                        diagnosticReport.addResult(new Reference(geneticVariant));
                    });
                }

                for (fhirspark.restmodel.Reference reference : therapyRecommendation.getReferences()) {
                    String title = reference.getName() != null ? reference.getName()
                            : pubmedResolver.resolvePublication(reference.getPmid());
                    Extension ex = new Extension().setUrl(RELATEDARTIFACT_URI);
                    RelatedArtifact relatedArtifact = new RelatedArtifact().setType(RelatedArtifactType.CITATION)
                            .setUrl(PUBMED_URI + reference.getPmid()).setCitation(title);
                    ex.setValue(relatedArtifact);
                    diagnosticReport.addExtension(ex);
                }

                for (Treatment treatment : therapyRecommendation.getTreatments()) {
                    Task medicationChange = new Task().setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL)
                            .setFor(fhirPatient);
                    medicationChange.setId(IdType.newRandomUuid());
                    medicationChange.getMeta().addProfile(MEDICATIONCHANGE_URI);

                    MedicationStatement ms = drugAdapter.process(fhirPatient, treatment);

                    medicationChange.getCode()
                            .addCoding(new Coding(LOINC_URI, "LA26421-0", "Consider alternative medication"));
                    medicationChange.setFocus(new Reference(ms));
                    String ncit = ms.getMedicationCodeableConcept().getCodingFirstRep().getCode();
                    medicationChange.addIdentifier(new Identifier().setSystem(NCIT_URI).setValue(ncit));

                    for (String comment : therapyRecommendation.getComment())
                        ms.getNote().add(new Annotation().setText(comment));

                    Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                    ex.setValue(new Reference(medicationChange));
                    diagnosticReport.addExtension(ex);

                    bundle.addEntry().setFullUrl(medicationChange.getIdElement().getValue())
                            .setResource(medicationChange).getRequest()
                            .setUrl("Task?identifier=" + NCIT_URI + "|" + ncit + "&subject="
                                    + fhirPatient.getResource().getIdElement())
                            .setIfNoneExist("identifier=Task?identifier=" + NCIT_URI + "|" + ncit + "&subject="
                                    + fhirPatient.getResource().getIdElement())
                            .setMethod(Bundle.HTTPVerb.PUT);
                }

                bundle.addEntry().setFullUrl(diagnosticReport.getIdElement().getValue()).setResource(diagnosticReport)
                        .getRequest()
                        .setUrl("DiagnosticReport?identifier=" + THERAPYRECOMMENDATION_URI + "|"
                                + therapyRecommendation.getId())
                        .setIfNoneExist("identifier=" + THERAPYRECOMMENDATION_URI + "|" + therapyRecommendation.getId())
                        .setMethod(Bundle.HTTPVerb.PUT);

            });

        });

        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

    }

    public void toHl7v2Oru(String patientId, String jsonString) throws HL7Exception, IOException, LLPException {
        List<Mtb> mtbs = this.objectMapper.readValue(jsonString, CbioportalRest.class).getMtbs();

        HapiContext context = new DefaultHapiContext();
        Connection connection = context.newClient(settings.getHl7v2config().get(0).getServer(),
                settings.getHl7v2config().get(0).getPort(), false);

        ORU_R01 oru = new ORU_R01();
        oru.initQuickstart("ORU", "R01", "P");

        ORU_R01_PATIENT_RESULT result = oru.insertPATIENT_RESULT(oru.getPATIENT_RESULTReps());
        result.getPATIENT().getPID().getPid1_SetIDPID().setValue("1");
        result.getPATIENT().getPID()
                .getPatientIdentifierList(result.getPATIENT().getPID().getPatientIdentifierListReps()).getIDNumber()
                .setValue(patientId);

        for (Mtb mtb : mtbs) {
            for (TherapyRecommendation therapyRecommendation : mtb.getTherapyRecommendations()) {
                int therapyRecommendationOrder = result.getORDER_OBSERVATIONReps();

                OBR masterPanel = result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBR();
                masterPanel.getSetIDOBR().setValue(String.valueOf(result.getORDER_OBSERVATIONReps()));
                masterPanel.getResultStatus().setValue("F");
                masterPanel.getUniversalServiceIdentifier().getIdentifier().setValue("81247-9");
                masterPanel.getUniversalServiceIdentifier().getText()
                        .setValue("Master HL7 genetic variant reporting panel");
                masterPanel.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LN");
                masterPanel.getUniversalServiceIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");

                masterPanel.getObservationDateTime().setValue(mtb.getDate().replaceAll("-", ""));

                for (String sample : mtb.getSamples()) {
                    SPM specimen = result.getORDER_OBSERVATION(therapyRecommendationOrder)
                            .getSPECIMEN(result.getORDER_OBSERVATION(therapyRecommendationOrder).getSPECIMENReps())
                            .getSPM();
                    specimen.getSetIDSPM().setValue(String.valueOf(
                            result.getORDER_OBSERVATION(result.getORDER_OBSERVATIONReps() - 1).getSPECIMENReps()));
                    specimen.getSpecimenID().getFillerAssignedIdentifier().getEntityIdentifier().setValue(sample);
                    specimen.getSpecimenType().getIdentifier().setValue("TUMOR");
                    specimen.getSpecimenType().getText().setValue("Tumor");
                }

                masterPanel.getFillerOrderNumber().getEntityIdentifier().setValue(mtb.getId());

                OBX evidence = result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBSERVATION(result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBSERVATIONReps()).getOBX();
                evidence.getSetIDOBX().setValue(String.valueOf(result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBSERVATIONReps()));
                evidence.getValueType().setValue("ST");
                evidence.getObservationIdentifier().getIdentifier().setValue("93044-6");
                evidence.getObservationIdentifier().getText().setValue("Level of evidence");
                evidence.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                evidence.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                ST evidenceValue = new ST(oru);
                evidenceValue.setValue(therapyRecommendation.getEvidenceLevel());
                evidence.insertObservationValue(0).setData(evidenceValue);

                for (GeneticAlteration g : therapyRecommendation.getReasoning().getGeneticAlterations()) {
                    int orderNumber = result.getORDER_OBSERVATIONReps();
                    OBR variant = result.insertORDER_OBSERVATION(orderNumber).getOBR();
                    variant.getSetIDOBR().setValue(String.valueOf(result.getORDER_OBSERVATIONReps()));
                    variant.getUniversalServiceIdentifier().getIdentifier().setValue("81250-3");
                    variant.getUniversalServiceIdentifier().getText().setValue("Discrete genetic variant panel");
                    variant.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LN");
                    variant.getUniversalServiceIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");


                    OBX observation = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(0).getOBX();
                    observation.getSetIDOBX().setValue(String.valueOf(1));
                    observation.getObservationIdentifier().getIdentifier().setValue("69548-6");
                    observation.getObservationIdentifier().getText().setValue("Genetic variant assessment");
                    observation.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation.getValueType().setValue("CWE");
                    CWE c0 = new CWE(oru);
                    c0.getNameOfCodingSystem().setValue("LN");
                    c0.getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    c0.getText().setValue("Present");
                    c0.getIdentifier().setValue("LA9633-4");
                    observation.insertObservationValue(0).setData(c0);

                    OBX observation0 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(1).getOBX();
                    observation0.getSetIDOBX().setValue(String.valueOf(2));
                    observation0.getObservationIdentifier().getIdentifier().setValue("48005-3");
                    observation0.getObservationIdentifier().getText().setValue("Amino acid change (pHGVS)");
                    observation0.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation0.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation0.getValueType().setValue("CWE");
                    CWE c1 = new CWE(oru);
                    c1.getCodingSystemOID().setValue("2.16.840.1.113883.6.282");
                    c1.getText().setValue("p." + g.getAlteration());
                    c1.getIdentifier().setValue("p." + g.getAlteration());
                    observation0.insertObservationValue(0).setData(c1);

                    OBX observation1 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(2).getOBX();
                    observation1.getSetIDOBX().setValue(String.valueOf(3));
                    observation1.getObservationIdentifier().getIdentifier().setValue("81252-9");
                    observation1.getObservationIdentifier().getText().setValue("Discrete genetic variant");
                    observation1.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation1.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation1.getValueType().setValue("CWE");
                    CWE c2 = new CWE(oru);
                    c2.getCodingSystemOID().setValue("2.16.840.1.113883.4.642.3.1041");
                    c2.getText().setValue(String.valueOf(g.getEntrezGeneId()));
                    c2.getIdentifier().setValue(String.valueOf(g.getEntrezGeneId()));
                    observation1.insertObservationValue(0).setData(c2);

                    OBX observation2 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(3).getOBX();
                    observation2.getSetIDOBX().setValue(String.valueOf(4));
                    observation2.getObservationIdentifier().getIdentifier().setValue("48018-6");
                    observation2.getObservationIdentifier().getText().setValue("Gene studied [ID]");
                    observation2.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation2.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation2.getValueType().setValue("CWE");
                    CWE c3 = new CWE(oru);
                    HgncGeneName hgncGeneName = new HgncGeneName();
                    Doc hgncData = hgncGeneName.resolve(g.getEntrezGeneId()).getResponse().getDocs().get(0);
                    c3.getCodingSystemOID().setValue("2.16.840.1.113883.6.281");
                    c3.getIdentifier().setValue(hgncData.getHgncId());
                    c3.getText().setValue(hgncData.getSymbol());
                    observation2.insertObservationValue(0).setData(c3);

                }

                NTE note = result.getORDER_OBSERVATION(therapyRecommendationOrder).getNTE();
                note.getSetIDNTE().setValue("1");
                note.getCommentType().getIdentifier().setValue("GI");
                note.getCommentType().getText().setValue("General Instructions");
                note.getComment(0).setValue(mtb.getGeneralRecommendation());

                for (int k = therapyRecommendationOrder; k < result.getORDER_OBSERVATIONReps(); k++)
                    for (ORU_R01_OBSERVATION observation : result.getORDER_OBSERVATION(k).getOBSERVATIONAll())
                        observation.getOBX().insertResponsibleObserver(0).getPersonIdentifier()
                                .setValue(therapyRecommendation.getAuthor());

            }

        }

        Message response = connection.getInitiator().sendAndReceive(oru.getMessage());

        System.out.println(oru.toString());
        System.out.println(response.encode());

        context.close();
    }

    public void deleteTherapyRecommendation(String patientId, String therapyRecommendationId) {
        assert (therapyRecommendationId.startsWith(patientId));
        client.delete().resourceConditionalByUrl("CarePlan?identifier=" + PATIENT_URI + "|" + therapyRecommendationId)
                .execute();
    }

    private Reference getOrCreatePatient(Bundle b, String patientId) {

        Patient patient = new Patient();
        patient.setId(IdType.newRandomUuid());
        patient.addIdentifier(new Identifier().setSystem(PATIENT_URI).setValue(patientId));
        b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest().setUrl("Patient")
                .setIfNoneExist("identifier=" + PATIENT_URI + "|" + patientId).setMethod(Bundle.HTTPVerb.POST);

        return new Reference(patient);
    }

    private Reference getOrCreatePractitioner(Bundle b, String credentials) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(IdType.newRandomUuid());
        practitioner.addIdentifier(new Identifier().setSystem(PATIENT_URI).setValue(credentials));
        b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                .setUrl("Practitioner").setIfNoneExist("identifier=" + PATIENT_URI + "|" + credentials)
                .setMethod(Bundle.HTTPVerb.POST);

        return new Reference(practitioner);

    }

    private String harmonizeId(IAnyResource resource) {
        if (resource.getIdElement().getValue().startsWith("urn:uuid:"))
            return resource.getIdElement().getValue();
        else
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
    }

}