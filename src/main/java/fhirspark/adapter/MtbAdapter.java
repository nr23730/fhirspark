package fhirspark.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import fhirspark.settings.Regex;

public class MtbAdapter {

    private static final String RECOMMENDEDACTION_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction";
    private static final String FOLLOWUP_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup";
    private static final String RELATEDARTIFACT_URI =
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RelatedArtifact";
    private static final String PUBMED_URI = "https://www.ncbi.nlm.nih.gov/pubmed/";

    public static Mtb toJson(IGenericClient client, List<Regex> regex, String patientId, DiagnosticReport diagnosticReport) {
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
                    applyRegexToCbioportal(regex, ((Specimen) specimen.getResource()).getIdentifierFirstRep().getValue()));
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

    private static String applyRegexToCbioportal(List<Regex> regex, String input) {
        String output = input;
        for (Regex r : regex) {
            output = output.replaceAll(r.getHis(), r.getCbio());
        }
        return output;
    }

}
