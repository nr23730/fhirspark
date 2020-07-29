package fhirspark;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fhirspark.adapter.DrugAdapter;
import fhirspark.adapter.GeneticAlterationsAdapter;
import fhirspark.adapter.SpecimenAdapter;
import fhirspark.adapter.clinicaldata.GenericAdapter;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.CbioportalRest;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.Deletions;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.r4.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

public class JsonFhirMapper {

    private static final String LOINC_URI = "http://loinc.org";
    private static final String PATIENT_URI = "https://cbioportal.org/patient/";
    private static final String RECOMMENDEDACTION_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction";
    private static final String FOLLOWUP_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup";
    private static final String RELATEDARTIFACT_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact";
    private static final String MEDICATIONCHANGE_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-med-chg";
    private static final String PUBMED_URI = "https://www.ncbi.nlm.nih.gov/pubmed/";
    private static final String NCIT_URI = "http://ncithesaurus-stage.nci.nih.gov";
    private static final String THERAPYRECOMMENDATION_URI = "https://cbioportal.org/therapyrecommendation/";
    private static final String MTB_URI = "https://cbioportal.org/mtb/";
    private static final String GENOMICSREPORT_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/genomics-report";
    private static final String GENOMIC_URI = "http://terminology.hl7.org/CodeSystem/v2-0074";

    private FhirContext ctx = FhirContext.forR4();
    private IGenericClient client;
    private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    private PubmedPublication pubmedResolver = new PubmedPublication();

    private GeneticAlterationsAdapter geneticAlterationsAdapter = new GeneticAlterationsAdapter();
    private DrugAdapter drugAdapter = new DrugAdapter();
    private SpecimenAdapter specimenAdapter = new SpecimenAdapter();

    public JsonFhirMapper(Settings settings) {
        this.client = ctx.newRestfulGenericClient(settings.getFhirDbBase());
    }

    public String toJson(String patientId) throws JsonProcessingException {
        List<Mtb> mtbs = new ArrayList<Mtb>();
        Bundle bPatient = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(PATIENT_URI, patientId)).prettyPrint()
                .execute();
        Patient fhirPatient = (Patient) bPatient.getEntryFirstRep().getResource();

        if (fhirPatient == null) {
            return this.objectMapper.writeValueAsString(new CbioportalRest().withId(patientId).withMtbs(mtbs));
        }

        Bundle bDiagnosticReports = (Bundle) client.search().forResource(DiagnosticReport.class)
                .where(new ReferenceClientParam("subject").hasId(harmonizeId(fhirPatient))).prettyPrint().execute();

        List<BundleEntryComponent> diagnosticReports = bDiagnosticReports.getEntry();

        for (int i = 0; i < diagnosticReports.size(); i++) {
            DiagnosticReport diagnosticReport = (DiagnosticReport) diagnosticReports.get(i).getResource();

            Mtb mtb = new Mtb().withTherapyRecommendations(new ArrayList<TherapyRecommendation>())
                    .withSamples(new ArrayList<String>());
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

            mtb.setId("mtb_" + patientId + "_" + diagnosticReport.getIssued().getTime());

            if (diagnosticReport.hasStatus()) {
                switch (diagnosticReport.getStatus().toCode()) {
                    case "final":
                        mtb.setMtbState("COMPLETED");
                        break;
                    case "cancelled":
                        mtb.setMtbState("ARCHIVED");
                        break;
                    default:
                    case "partial":
                        mtb.setMtbState("DRAFT");
                        break;
                }
            }

            // REBIOPSY HERE

            mtb.getSamples().clear();
            for (Reference specimen : diagnosticReport.getSpecimen()) {
                mtb.getSamples().add(((Specimen) specimen.getResource()).getIdentifierFirstRep().getValue());
            }

            for (Reference reference : diagnosticReport.getResult()) {
                switch (reference.getResource().getMeta().getProfile().get(0).getValue()) {
                    case "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medication-efficacy":
                        Observation ob = (Observation) reference.getResource();

                        TherapyRecommendation therapyRecommendation = new TherapyRecommendation()
                                .withComment(new ArrayList<String>()).withReasoning(new Reasoning());
                        mtb.getTherapyRecommendations().add(therapyRecommendation);
                        List<ClinicalDatum> clinicalData = new ArrayList<ClinicalDatum>();
                        List<GeneticAlteration> geneticAlterations = new ArrayList<GeneticAlteration>();
                        therapyRecommendation.getReasoning().withClinicalData(clinicalData)
                                .withGeneticAlterations(geneticAlterations);

                        therapyRecommendation.setAuthor(mtb.getAuthor());

                        therapyRecommendation.setId(ob.getIdentifierFirstRep().getValue());

                        ob.getHasMember().forEach(member -> {
                            Observation obs = (Observation) member.getResource();
                            String[] attr = obs.getValueStringType().asStringValue().split(": ");
                            therapyRecommendation.getReasoning().getClinicalData()
                                    .add(new ClinicalDatum().withAttributeName(attr[0]).withValue(attr[1]));
                        });

                        List<Treatment> treatments = new ArrayList<Treatment>();
                        therapyRecommendation.setTreatments(treatments);
                        List<Extension> recommendedActionReferences = diagnosticReport
                                .getExtensionsByUrl(RECOMMENDEDACTION_URI);

                        recommendedActionReferences.forEach(recommendedActionReference -> {

                            Task t = (Task) ((Reference) recommendedActionReference.getValue()).getResource();
                            if (t != null) {
                                assert t.getMeta().getProfile().get(0).getValue().equals(FOLLOWUP_URI);
                                Coding c = t.getCode().getCodingFirstRep();
                                switch (c.getCode()) {
                                    case "LA14021-2":
                                        mtb.setRebiopsyRecommendation(true);
                                        break;
                                    case "LA14020-4":
                                        mtb.setGeneticCounselingRecommendation(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                        List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
                        ob.getExtensionsByUrl(RELATEDARTIFACT_URI).forEach(relatedArtifact -> {
                            if (((RelatedArtifact) relatedArtifact.getValue())
                                    .getType() == RelatedArtifactType.CITATION) {
                                references.add(new fhirspark.restmodel.Reference()
                                        .withPmid(Integer.valueOf(((RelatedArtifact) relatedArtifact.getValue())
                                                .getUrl().replaceFirst(PUBMED_URI, "")))
                                        .withName(((RelatedArtifact) relatedArtifact.getValue()).getCitation()));
                            }
                        });

                        therapyRecommendation.setReferences(references);

                        ob.getComponent().forEach(result -> {
                            if (result.getCode().getCodingFirstRep().getCode().equals("93044-6")) {
                                therapyRecommendation.setEvidenceLevel(
                                        result.getValueCodeableConcept().getCodingFirstRep().getCode());
                            }
                            if (result.getCode().getCodingFirstRep().getCode().equals("51963-7")) {
                                therapyRecommendation.getTreatments().add(new Treatment()
                                        .withNcitCode(result.getValueCodeableConcept().getCodingFirstRep().getCode())
                                        .withName(result.getValueCodeableConcept().getCodingFirstRep().getDisplay()));
                            }
                        });

                        ob.getDerivedFrom().forEach(reference1 -> {
                            GeneticAlteration g = new GeneticAlteration();
                            ((Observation) reference1.getResource()).getComponent().forEach(variant -> {
                                switch (variant.getCode().getCodingFirstRep().getCode()) {
                                    case "48005-3":
                                        g.setAlteration(variant.getValueCodeableConcept().getCodingFirstRep().getCode()
                                                .replaceFirst("p.", ""));
                                        break;
                                    case "81252-9":
                                        variant.getValueCodeableConcept().getCoding().forEach(coding -> {
                                            switch (coding.getSystem()) {
                                                case "http://www.ncbi.nlm.nih.gov/gene":
                                                    g.setEntrezGeneId(Integer.valueOf(coding.getCode()));
                                                    break;
                                                case "http://www.ncbi.nlm.nih.gov/clinvar":
                                                    g.setClinvar(Integer.valueOf(coding.getCode()));
                                                    break;
                                                case "http://cancer.sanger.ac.uk/cancergenome/projects/cosmic":
                                                    g.setCosmic(coding.getCode());
                                                    break;
                                                default:
                                                    break;
                                            }
                                        });
                                        break;
                                    case "48018-6":
                                        g.setHugoSymbol(
                                                variant.getValueCodeableConcept().getCodingFirstRep().getDisplay());
                                        break;
                                    case "48001-2":
                                        g.setChromosome(
                                                variant.getValueCodeableConcept().getCodingFirstRep().getCode());
                                        break;
                                    case "81258-6":
                                        g.setAlleleFrequency(
                                                variant.getValueQuantity().getValue().doubleValue());
                                        break;
                                    case "81255-2":
                                        g.setDbsnp(variant.getValueCodeableConcept().getCodingFirstRep().getCode());
                                        break;
                                    case "62378-5":
                                        switch (variant.getValueCodeableConcept().getCodingFirstRep().getCode()) {
                                            case "LA14033-7":
                                                g.setAlteration("Amplification");
                                                break;
                                            case "LA14034-5":
                                                g.setAlteration("Deletion");
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                    case "69551-0":
                                        g.setAlt(variant.getValueStringType().getValue());
                                        break;
                                    case "69547-8":
                                        g.setRef(variant.getValueStringType().getValue());
                                        break;
                                    case "exact-start-end":
                                        if (variant.getValueRange().getLow().getValue() != null) {
                                            g.setStart(Integer
                                                    .valueOf(variant.getValueRange().getLow().getValue().toString()));
                                        }
                                        if (variant.getValueRange().getHigh().getValue() != null) {
                                            g.setEnd(Integer
                                                    .valueOf(variant.getValueRange().getHigh().getValue().toString()));
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            });
                            geneticAlterations.add(g);
                        });

                        ob.getNote().forEach(note -> therapyRecommendation.getComment().add(note.getText()));
                        break;
                    default:
                        break;
                }
            }

        }

        return this.objectMapper.writeValueAsString(new CbioportalRest().withId(patientId).withMtbs(mtbs));

    }

    public void addOrEditMtb(String patientId, List<Mtb> mtbs) throws DataFormatException, IOException {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        Reference fhirPatient = getOrCreatePatient(bundle, patientId);

        for (Mtb mtb : mtbs) {

            DiagnosticReport diagnosticReport = new DiagnosticReport();
            diagnosticReport.getMeta().addProfile(GENOMICSREPORT_URI);
            diagnosticReport.setId(IdType.newRandomUuid());
            diagnosticReport.setSubject(fhirPatient);
            diagnosticReport.addCategory().addCoding(new Coding().setSystem(GENOMIC_URI).setCode("GE"));
            diagnosticReport.getCode()
                    .addCoding(new Coding(LOINC_URI, "81247-9", "Master HL7 genetic variant reporting panel"));

            CarePlan carePlan = new CarePlan();
            carePlan.setId(IdType.newRandomUuid());
            carePlan.setSubject(fhirPatient);
            carePlan.setIntent(CarePlanIntent.PROPOSAL);
            carePlan.setStatus(CarePlanStatus.ACTIVE);
            carePlan.setAuthor(getOrCreatePractitioner(bundle, mtb.getAuthor()));
            carePlan.getSupportingInfo().add(new Reference(diagnosticReport));

            diagnosticReport.addPerformer(getOrCreatePractitioner(bundle, mtb.getAuthor()));

            diagnosticReport.getEffectiveDateTimeType().fromStringValue(mtb.getDate());

            diagnosticReport.setConclusion(mtb.getGeneralRecommendation());

            diagnosticReport.addIdentifier().setSystem(MTB_URI).setValue(mtb.getId());

            if (mtb.getGeneticCounselingRecommendation() != null && mtb.getGeneticCounselingRecommendation()) {
                Task t = new Task();
                t.getMeta().addProfile(FOLLOWUP_URI);
                t.setFor(fhirPatient);
                t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                t.getCode().setText("Recommended follow-up")
                        .addCoding(new Coding(LOINC_URI, "LA14020-4", "Genetic counseling recommended"));
                Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                ex.setValue(new Reference(t));
                diagnosticReport.addExtension(ex);
                carePlan.getActivity().add(new CarePlanActivityComponent().setReference(new Reference(t)));
            }

            assert mtb.getId().startsWith("mtb_" + patientId + "_");
            diagnosticReport.setIssued(new Date(Long.valueOf(mtb.getId().replace("mtb_" + patientId + "_", ""))));

            if (mtb.getMtbState() != null) {
                switch (mtb.getMtbState().toUpperCase()) {
                    case "COMPLETED":
                        diagnosticReport.setStatus(DiagnosticReportStatus.FINAL);
                        break;
                    case "ARCHIVED":
                        diagnosticReport.setStatus(DiagnosticReportStatus.CANCELLED);
                        break;
                    default:
                    case "DRAFT":
                        diagnosticReport.setStatus(DiagnosticReportStatus.PARTIAL);
                        break;
                }
            } else {
                diagnosticReport.setStatus(DiagnosticReportStatus.PARTIAL);
            }

            if (mtb.getRebiopsyRecommendation() != null && mtb.getRebiopsyRecommendation()) {
                Task t = new Task();
                t.getMeta().addProfile(FOLLOWUP_URI);
                t.setFor(fhirPatient);
                t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
                t.getCode().setText("Recommended follow-up")
                        .addCoding(new Coding(LOINC_URI, "LA14021-2", "Confirmatory testing recommended"));
                Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                ex.setValue(new Reference(t));
                diagnosticReport.addExtension(ex);
            }

            mtb.getSamples().forEach(sample -> diagnosticReport
                    .addSpecimen(new Reference(specimenAdapter.process(fhirPatient, sample))));

            for (TherapyRecommendation therapyRecommendation : mtb.getTherapyRecommendations()) {
                Observation efficacyObservation = new Observation();
                diagnosticReport.addResult(new Reference(efficacyObservation));
                efficacyObservation.getMeta().addProfile(
                        "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medication-efficacy");
                efficacyObservation.setStatus(ObservationStatus.FINAL);
                efficacyObservation.addCategory().addCoding(new Coding(ObservationCategory.LABORATORY.getSystem(),
                        ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay()));
                efficacyObservation.getValueCodeableConcept()
                        .addCoding(new Coding(LOINC_URI, "LA9661-5", "Presumed responsive"));
                efficacyObservation.getCode()
                        .addCoding(new Coding(LOINC_URI, "51961-1", "Genetic variation's effect on drug efficacy"));
                ObservationComponentComponent evidenceComponent = efficacyObservation.addComponent();
                evidenceComponent.getCode().addCoding(new Coding(LOINC_URI, "93044-6", "Level of evidence"));
                evidenceComponent.getValueCodeableConcept().addCoding(new Coding("https://cbioportal.org/evidence/BW/",
                        therapyRecommendation.getEvidenceLevel(), therapyRecommendation.getEvidenceLevel()));

                efficacyObservation.addIdentifier().setSystem(THERAPYRECOMMENDATION_URI)
                        .setValue(therapyRecommendation.getId());

                efficacyObservation.addPerformer(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor()));

                therapyRecommendation.getComment()
                        .forEach(comment -> efficacyObservation.getNote().add(new Annotation().setText(comment)));

                if (therapyRecommendation.getReasoning().getClinicalData() != null) {
                    therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
                        try {
                            Method m = Class.forName("fhirspark.adapter.clinicaldata." + clinical.getAttributeId())
                                    .getMethod("process", ClinicalDatum.class);
                            efficacyObservation.addHasMember(new Reference((Resource) m.invoke(null, clinical)));
                        } catch (ClassNotFoundException e) {
                            GenericAdapter genericAdapter = new GenericAdapter();
                            efficacyObservation.addHasMember(new Reference(genericAdapter.process(clinical)));
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
                        efficacyObservation.addDerivedFrom(new Reference(geneticVariant));
                    });
                }

                if (therapyRecommendation.getReferences() != null) {
                    therapyRecommendation.getReferences().forEach(reference -> {
                        String title = reference.getName() != null ? reference.getName()
                                : pubmedResolver.resolvePublication(reference.getPmid());
                        Extension ex = new Extension().setUrl(RELATEDARTIFACT_URI);
                        RelatedArtifact relatedArtifact = new RelatedArtifact().setType(RelatedArtifactType.CITATION)
                                .setUrl(PUBMED_URI + reference.getPmid()).setCitation(title);
                        ex.setValue(relatedArtifact);
                        efficacyObservation.addExtension(ex);
                    });
                }

                if (therapyRecommendation.getTreatments() != null) {
                    therapyRecommendation.getTreatments().forEach(treatment -> {
                        Task medicationChange = new Task().setStatus(TaskStatus.REQUESTED)
                                .setIntent(TaskIntent.PROPOSAL).setFor(fhirPatient);
                        medicationChange.setId(IdType.newRandomUuid());
                        medicationChange.getMeta().addProfile(MEDICATIONCHANGE_URI);

                        MedicationStatement ms = drugAdapter.process(fhirPatient, treatment);

                        medicationChange.getCode()
                                .addCoding(new Coding(LOINC_URI, "LA26421-0", "Consider alternative medication"));
                        medicationChange.setFocus(new Reference(ms));
                        String ncit = ms.getMedicationCodeableConcept().getCodingFirstRep().getCode();
                        medicationChange.addIdentifier(new Identifier().setSystem(NCIT_URI).setValue(ncit));

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

                        ObservationComponentComponent assessed = efficacyObservation.addComponent();
                        assessed.getCode().addCoding(new Coding(LOINC_URI, "51963-7", "Medication assessed [ID]"));
                        assessed.setValue(ms.getMedicationCodeableConcept());

                        carePlan.addActivity().setReference(new Reference(medicationChange));
                    });
                }

            }

            bundle.addEntry().setFullUrl(diagnosticReport.getIdElement().getValue()).setResource(diagnosticReport)
                    .getRequest().setUrl("DiagnosticReport?identifier=" + MTB_URI + "|" + mtb.getId())
                    .setIfNoneExist("identifier=" + MTB_URI + "|" + mtb.getId()).setMethod(Bundle.HTTPVerb.PUT);

        }

        try {
            Bundle resp = client.transaction().withBundle(bundle).execute();

            // Log the response
            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));
        } catch (UnprocessableEntityException entityException) {
            FileWriter f = new FileWriter("error.json");
            f.write(entityException.getResponseBody());
            f.close();
        }

    }

    public void deleteEntries(String patientId, Deletions deletions) {
        // deletions.getTherapyRecommendation()
        // .forEach(recommendation -> deleteTherapyRecommendation(patientId,
        // recommendation));
        deletions.getMtb().forEach(mtb -> deleteMtb(patientId, mtb));
    }

    private void deleteTherapyRecommendation(String patientId, String therapyRecommendationId) {
        assert therapyRecommendationId.startsWith(patientId);
        client.delete()
                .resourceConditionalByUrl(
                        "Observation?identifier=" + THERAPYRECOMMENDATION_URI + "|" + therapyRecommendationId)
                .execute();
    }

    private void deleteMtb(String patientId, String mtbId) {
        assert mtbId.startsWith("mtb_" + patientId + "_");
        client.delete().resourceConditionalByUrl("DiagnosticReport?identifier=" + MTB_URI + "|" + mtbId).execute();
    }

    private Reference getOrCreatePatient(Bundle b, String patientId) {

        Patient patient = new Patient();
        patient.setId(IdType.newRandomUuid());
        patient.getIdentifierFirstRep().setSystem(PATIENT_URI).setValue(patientId);
        patient.getIdentifierFirstRep().setUse(IdentifierUse.USUAL);
        patient.getIdentifierFirstRep().getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("MR");
        b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest()
                .setUrl("Patient?identifier=" + PATIENT_URI + "|" + patientId)
                .setIfNoneExist("identifier=" + PATIENT_URI + "|" + patientId).setMethod(Bundle.HTTPVerb.PUT);

        return new Reference(patient);
    }

    private Reference getOrCreatePractitioner(Bundle b, String credentials) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(IdType.newRandomUuid());
        practitioner.addIdentifier(new Identifier().setSystem(PATIENT_URI).setValue(credentials));
        b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                .setUrl("Practitioner?identifier=" + PATIENT_URI + "|" + credentials)
                .setIfNoneExist("identifier=" + PATIENT_URI + "|" + credentials).setMethod(Bundle.HTTPVerb.PUT);

        return new Reference(practitioner);

    }

    private String harmonizeId(IAnyResource resource) {
        if (resource.getIdElement().getValue().startsWith("urn:uuid:")) {
            return resource.getIdElement().getValue();
        } else {
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
        }
    }

}
