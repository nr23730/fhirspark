package fhirspark.adapter;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.adapter.clinicaldata.GenericAdapter;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import fhirspark.settings.Regex;
import fhirspark.settings.Settings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.ServiceRequest.ServiceRequestStatus;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

public class MtbAdapter {

    private static final String LOINC_URI = "http://loinc.org";
    private static final String RECOMMENDEDACTION_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction";
    private static final String FOLLOWUP_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup";
    private static final String RELATEDARTIFACT_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact";
    private static final String MEDICATIONCHANGE_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-med-chg";
    private static final String PUBMED_URI = "https://www.ncbi.nlm.nih.gov/pubmed/";
    private static final String NCIT_URI = "http://ncithesaurus-stage.nci.nih.gov";
    private static final String GENOMICSREPORT_URI = "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/genomics-report";
    private static final String GENOMIC_URI = "http://terminology.hl7.org/CodeSystem/v2-0074";

    private static PubmedPublication pubmedResolver = new PubmedPublication();

    private static GeneticAlterationsAdapter geneticAlterationsAdapter = new GeneticAlterationsAdapter();
    private static DrugAdapter drugAdapter = new DrugAdapter();
    private static SpecimenAdapter specimenAdapter;
    private static IGenericClient client;
    private static String patientUri;
    private static String therapyRecommendationUri;
    private static String mtbUri;
    private static String serviceRequestUri;

    public static void initialize(Settings settings, IGenericClient client) {
        MtbAdapter.specimenAdapter = new SpecimenAdapter(settings.getSpecimenSystem());
        MtbAdapter.client = client;
        MtbAdapter.patientUri = settings.getPatientSystem();
        MtbAdapter.therapyRecommendationUri = settings.getObservationSystem();
        MtbAdapter.mtbUri = settings.getDiagnosticReportSystem();
        MtbAdapter.serviceRequestUri = settings.getServiceRequestSystem();
    }

    public static Mtb toJson(List<Regex> regex, String patientId,
            DiagnosticReport diagnosticReport) {
        Mtb mtb = new Mtb().withTherapyRecommendations(new ArrayList<TherapyRecommendation>())
                .withSamples(new ArrayList<String>());

        if (diagnosticReport.hasBasedOn()) {
            mtb.setOrderId(((ServiceRequest) diagnosticReport.getBasedOnFirstRep().getResource())
                    .getIdentifierFirstRep().getValue());
        }

        if (diagnosticReport.hasPerformer()) {
            Bundle b2 = (Bundle) client.search().forResource(Practitioner.class).where(new TokenClientParam("_id")
                    .exactly().code(diagnosticReport.getPerformerFirstRep().getReference())).prettyPrint()
                    .execute();
            Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();
            mtb.setAuthor(author.getIdentifierFirstRep().getValue());
        }

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        mtb.setDate(f.format(diagnosticReport.getEffectiveDateTimeType().toCalendar().getTime()));

        mtb.setGeneralRecommendation(diagnosticReport.getConclusion());

        // GENETIC COUNSELING HERE

        mtb.setId("mtb_" + patientId + "_" + diagnosticReport.getIssued().getTime());

        if (diagnosticReport.hasStatus()) {
            mtb.setMtbState(diagnosticReport.getStatus().toCode().toUpperCase());
        }

        // REBIOPSY HERE
        mtb.getSamples().clear();
        for (Reference specimen : diagnosticReport.getSpecimen()) {
            mtb.getSamples().add(
                    applyRegexToCbioportal(regex,
                            ((Specimen) specimen.getResource()).getIdentifierFirstRep().getValue()));
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

                    if (ob.hasPerformer()) {
                        Bundle b2 = (Bundle) client
                                .search().forResource(Practitioner.class).where(new TokenClientParam("_id")
                                        .exactly().code(ob.getPerformerFirstRep().getReference()))
                                .prettyPrint().execute();
                        Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();
                        therapyRecommendation.setAuthor(author.getIdentifierFirstRep().getValue());
                    }

                    therapyRecommendation.setId(ob.getIdentifierFirstRep().getValue());

                    ob.getHasMember().forEach(member -> {
                        Observation obs = (Observation) member.getResource();
                        String[] attr = obs.getValueStringType().asStringValue().split(": ");
                        ClinicalDatum cd = new ClinicalDatum().withAttributeName(attr[0]).withValue(attr[1]);
                        if (obs.getSpecimen().getResource() != null) {
                            Specimen specimen = (Specimen) obs.getSpecimen().getResource();
                            cd.setSampleId(applyRegexToCbioportal(regex, specimen.getIdentifierFirstRep().getValue()));
                        }
                        therapyRecommendation.getReasoning().getClinicalData()
                                .add(cd);
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
                            String[] evidence = result.getValueCodeableConcept().getCodingFirstRep().getCode()
                                    .split(" ");
                            therapyRecommendation.setEvidenceLevel(evidence[0]);
                            if (evidence.length > 1) {
                                therapyRecommendation.setEvidenceLevelExtension(evidence[1]);
                            }
                            if (evidence.length > 2) {
                                therapyRecommendation.setEvidenceLevelM3Text(
                                        String.join(" ", Arrays.asList(evidence).subList(2, evidence.length))
                                                .replace("(", "").replace(")", ""));
                            }
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
                                    g.setAlleleFrequency(variant.getValueQuantity().getValue().doubleValue());
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

        return mtb;

    }

    public static void fromJson(Bundle bundle, List<Regex> regex, Reference fhirPatient, String patientId, Mtb mtb) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.getMeta().addProfile(GENOMICSREPORT_URI);
        diagnosticReport.setId(IdType.newRandomUuid());
        diagnosticReport.setSubject(fhirPatient);
        diagnosticReport.addCategory().addCoding(new Coding().setSystem(GENOMIC_URI).setCode("GE"));
        diagnosticReport.getCode()
                .addCoding(new Coding(LOINC_URI, "81247-9", "Master HL7 genetic variant reporting panel"));

        if (mtb.getOrderId() != null && !mtb.getOrderId().isEmpty()) {
            ServiceRequest sr = new ServiceRequest();
            sr.setId(IdType.newRandomUuid());
            sr.addIdentifier().setSystem(serviceRequestUri).setValue(mtb.getOrderId());
            sr.setSubject(fhirPatient);
            sr.setStatus(
                    DiagnosticReportStatus.fromCode(mtb.getMtbState().toLowerCase())
                            .equals(DiagnosticReportStatus.FINAL)
                                    ? ServiceRequestStatus.COMPLETED
                                    : ServiceRequestStatus.DRAFT);
            bundle.addEntry().setFullUrl(sr.getIdElement().getValue()).setResource(sr).getRequest()
                    .setUrl("ServiceRequest?identifier=" + serviceRequestUri + "|" + mtb.getOrderId())
                    .setIfNoneExist("identifier=" + serviceRequestUri
                            + "|" + mtb.getOrderId())
                    .setMethod(Bundle.HTTPVerb.PUT);
            diagnosticReport.addBasedOn(new Reference(sr));
        }

        diagnosticReport.addPerformer(getOrCreatePractitioner(bundle, mtb.getAuthor()));

        diagnosticReport.getEffectiveDateTimeType().fromStringValue(mtb.getDate());

        diagnosticReport.setConclusion(mtb.getGeneralRecommendation());

        diagnosticReport.addIdentifier().setSystem(mtbUri).setValue(mtb.getId());

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
        }

        assert mtb.getId().startsWith("mtb_" + patientId + "_");
        diagnosticReport.setIssued(new Date(Long.valueOf(mtb.getId().replace("mtb_" + patientId + "_", ""))));

        if (mtb.getMtbState() != null) {
            diagnosticReport.setStatus(DiagnosticReportStatus.fromCode(mtb.getMtbState().toLowerCase()));
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

        mtb.getSamples().forEach(sample -> {
            String sampleId = applyRegexFromCbioportal(regex, sample);
            Specimen s = specimenAdapter.fromJson(fhirPatient, sampleId);
            bundle.addEntry().setFullUrl(s.getIdElement().getValue()).setResource(s)
                    .getRequest().setUrl("Specimen?identifier=https://cbioportal.org/specimen/|" + sampleId)
                    .setIfNoneExist("identifier=identifier=https://cbioportal.org/specimen/|" + sampleId)
                    .setMethod(Bundle.HTTPVerb.PUT);
            diagnosticReport.addSpecimen(new Reference(s));
        });

        for (TherapyRecommendation therapyRecommendation : mtb.getTherapyRecommendations()) {
            Observation efficacyObservation = new Observation();
            efficacyObservation.setId(IdType.newRandomUuid());
            bundle.addEntry().setFullUrl(efficacyObservation.getIdElement().getValue())
                    .setResource(efficacyObservation).getRequest()
                    .setUrl("Observation?identifier=" + therapyRecommendationUri + "|"
                            + therapyRecommendation.getId())
                    .setIfNoneExist("identifier=" + therapyRecommendationUri + "|" + therapyRecommendation.getId())
                    .setMethod(Bundle.HTTPVerb.PUT);
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
            String m3Text = therapyRecommendation.getEvidenceLevelM3Text() != null
                    ? " (" + therapyRecommendation.getEvidenceLevelM3Text() + ")"
                    : "";
            evidenceComponent.getValueCodeableConcept().addCoding(new Coding("https://cbioportal.org/evidence/BW/",
                    therapyRecommendation.getEvidenceLevel() + " "
                            + therapyRecommendation.getEvidenceLevelExtension() + m3Text,
                    therapyRecommendation.getEvidenceLevel() + " "
                            + therapyRecommendation.getEvidenceLevelExtension() + m3Text));

            efficacyObservation.addIdentifier().setSystem(therapyRecommendationUri)
                    .setValue(therapyRecommendation.getId());

            efficacyObservation.addPerformer(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor()));

            therapyRecommendation.getComment()
                    .forEach(comment -> efficacyObservation.getNote().add(new Annotation().setText(comment)));

            if (therapyRecommendation.getReasoning().getClinicalData() != null) {
                therapyRecommendation.getReasoning().getClinicalData().forEach(clinical -> {
                    Specimen s = null;
                    if (clinical.getSampleId() != null && clinical.getSampleId().length() > 0) {
                        String sampleId = applyRegexFromCbioportal(regex, clinical.getSampleId());
                        s = specimenAdapter.fromJson(fhirPatient, sampleId);
                        bundle.addEntry().setFullUrl(s.getIdElement().getValue()).setResource(s)
                                .getRequest().setUrl("Specimen?identifier=https://cbioportal.org/specimen/|"
                                        + sampleId)
                                .setIfNoneExist("identifier=identifier=https://cbioportal.org/specimen/|"
                                        + sampleId)
                                .setMethod(Bundle.HTTPVerb.PUT);
                    }
                    try {
                        Method m = Class.forName("fhirspark.adapter.clinicaldata." + clinical.getAttributeId())
                                .getMethod("process", ClinicalDatum.class);
                        efficacyObservation.addHasMember(new Reference((Resource) m.invoke(null, clinical)));
                    } catch (ClassNotFoundException e) {
                        GenericAdapter genericAdapter = new GenericAdapter();
                        efficacyObservation
                                .addHasMember(new Reference(genericAdapter.fromJson(clinical, new Reference(s))));
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
                    String uniqueString = "component-value-concept=http://www.ncbi.nlm.nih.gov/gene|"
                            + geneticAlteration.getEntrezGeneId() + "&subject="
                            + fhirPatient.getResource().getIdElement();
                    Observation geneticVariant = geneticAlterationsAdapter.fromJson(geneticAlteration);
                    geneticVariant.setId(IdType.newRandomUuid());
                    geneticVariant.setSubject(fhirPatient);
                    bundle.addEntry().setFullUrl(geneticVariant.getIdElement().getValue())
                            .setResource(geneticVariant).getRequest()
                            .setUrl("Observation?" + uniqueString)
                            .setIfNoneExist(uniqueString)
                            .setMethod(Bundle.HTTPVerb.PUT);
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

                    MedicationStatement ms = drugAdapter.fromJson(fhirPatient, treatment);

                    medicationChange.getCode()
                            .addCoding(new Coding(LOINC_URI, "LA26421-0", "Consider alternative medication"));
                    medicationChange.setFocus(new Reference(ms));
                    String ncit = ms.getMedicationCodeableConcept().getCodingFirstRep().getCode();
                    if (ncit == null) {
                        ncit = treatment.getName();
                    }
                    medicationChange.addIdentifier(new Identifier().setSystem(NCIT_URI).setValue(ncit));

                    Extension ex = new Extension().setUrl(RECOMMENDEDACTION_URI);
                    ex.setValue(new Reference(medicationChange));
                    diagnosticReport.addExtension(ex);

                    bundle.addEntry().setFullUrl(medicationChange.getIdElement().getValue())
                            .setResource(medicationChange).getRequest()
                            .setUrl("Task?identifier=" + NCIT_URI + "|" + ncit + "&subject="
                                    + fhirPatient.getResource().getIdElement())
                            .setIfNoneExist("identifier=" + NCIT_URI + "|" + ncit + "&subject="
                                    + fhirPatient.getResource().getIdElement())
                            .setMethod(Bundle.HTTPVerb.PUT);

                    ObservationComponentComponent assessed = efficacyObservation.addComponent();
                    assessed.getCode().addCoding(new Coding(LOINC_URI, "51963-7", "Medication assessed [ID]"));
                    assessed.setValue(ms.getMedicationCodeableConcept());

                });
            }

        }

        bundle.addEntry().setFullUrl(diagnosticReport.getIdElement().getValue()).setResource(diagnosticReport)
                .getRequest().setUrl("DiagnosticReport?identifier=" + mtbUri + "|" + mtb.getId())
                .setIfNoneExist("identifier=" + mtbUri + "|" + mtb.getId()).setMethod(Bundle.HTTPVerb.PUT);

    }

    private static String applyRegexToCbioportal(List<Regex> regex, String input) {
        String output = input;
        for (Regex r : regex) {
            output = output.replaceAll(r.getHis(), r.getCbio());
        }
        return output;
    }

    private static String applyRegexFromCbioportal(List<Regex> regex, String input) {
        String output = input;
        for (Regex r : regex) {
            output = output.replaceAll(r.getCbio(), r.getHis());
        }
        return output;
    }

    private static Reference getOrCreatePractitioner(Bundle b, String credentials) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(IdType.newRandomUuid());
        practitioner.addIdentifier(new Identifier().setSystem(patientUri).setValue(credentials));
        b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                .setUrl("Practitioner?identifier=" + patientUri + "|" + credentials)
                .setIfNoneExist("identifier=" + patientUri + "|" + credentials).setMethod(Bundle.HTTPVerb.PUT);

        return new Reference(practitioner);

    }

}
