package fhirspark.adapter;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.LoincEnum;
import fhirspark.definitions.MolekulargenetischerBefundberichtEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import fhirspark.settings.Regex;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class TherapyRecommendationAdapter {

    private static PubmedPublication pubmedResolver = new PubmedPublication();
    private static String therapyRecommendationUri;
    private static String patientUri;

    private TherapyRecommendationAdapter() {
    }

    public static void initialize(String newTherapyRecommendationUri, String newPatientUri) {
        TherapyRecommendationAdapter.therapyRecommendationUri = newTherapyRecommendationUri;
        TherapyRecommendationAdapter.patientUri = newPatientUri;
    }

    public static Observation fromJson(Bundle bundle, List<Regex> regex, DiagnosticReport diagnosticReport,
            Reference fhirPatient, TherapyRecommendation therapyRecommendation, Map<String, Observation> unique) {
        Observation therapeuticImplication = new Observation();
        therapeuticImplication.setId(IdType.newRandomUuid());
        therapeuticImplication.getMeta().addProfile(GenomicsReportingEnum.THERAPEUTIC_IMPLICATION.getSystem());
        therapeuticImplication.getMeta()
                .addProfile(MolekulargenetischerBefundberichtEnum.THERAPEUTIC_IMPLICATION.getSystem());
        therapeuticImplication.setStatus(ObservationStatus.FINAL);
        therapeuticImplication.setSubject(fhirPatient);
        therapeuticImplication.addCategory().addCoding(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay()));
        therapeuticImplication.getCode()
                .addCoding(GenomicsReportingEnum.THERAPEUTIC_IMPLICATION_CODING.toCoding());
        ObservationComponentComponent evidenceComponent = therapeuticImplication.addComponent();
        evidenceComponent.getCode().addCoding(LoincEnum.LEVEL_OF_EVIDENCE.toCoding());
        String m3Text = therapyRecommendation.getEvidenceLevelM3Text() != null
                && !therapyRecommendation.getEvidenceLevelM3Text().isEmpty()
                        ? " (" + therapyRecommendation.getEvidenceLevelM3Text() + ")"
                        : "";
        String evidenceLevelCode = therapyRecommendation.getEvidenceLevel();
        String evidenceLevelDisplay = therapyRecommendation.getEvidenceLevel();
        if (therapyRecommendation.getEvidenceLevelExtension() != null
                && !"null".equals(therapyRecommendation.getEvidenceLevelExtension())) {
            evidenceLevelCode += "_" + therapyRecommendation.getEvidenceLevelExtension() + m3Text;
            evidenceLevelDisplay += " " + therapyRecommendation.getEvidenceLevelExtension() + m3Text;
        }

        evidenceComponent.getValueCodeableConcept().addCoding(new Coding("https://cbioportal.org/evidence/BW/",
                evidenceLevelCode, evidenceLevelDisplay));

        therapeuticImplication.addIdentifier().setSystem(therapyRecommendationUri)
                .setValue(therapyRecommendation.getId());

        therapeuticImplication.addPerformer(getOrCreatePractitioner(bundle, therapyRecommendation.getAuthor()));

        therapyRecommendation.getComment()
                .forEach(comment -> therapeuticImplication.getNote()
                        .add(new Annotation().setText(comment)));

        if (therapyRecommendation.getReasoning() != null) {
            ReasoningAdapter.fromJson(bundle, therapeuticImplication, regex,
                    fhirPatient, therapyRecommendation.getReasoning(), unique);
        }

        if (therapyRecommendation.getReferences() != null) {
            therapyRecommendation.getReferences().forEach(reference -> {
                String title = reference.getName() != null ? reference.getName()
                        : pubmedResolver.resolvePublication(reference.getPmid());
                Extension ex = new Extension()
                        .setUrl(GenomicsReportingEnum.RELATEDARTIFACT.getSystem());
                RelatedArtifact relatedArtifact = new RelatedArtifact()
                        .setType(RelatedArtifactType.CITATION)
                        .setUrl(UriEnum.PUBMED_URI.getUri() + reference.getPmid())
                        .setCitation(title);
                ex.setValue(relatedArtifact);
                therapeuticImplication.addExtension(ex);
            });
        }

        if (therapyRecommendation.getTreatments() != null) {
            therapyRecommendation.getTreatments().forEach(treatment -> {
                therapeuticImplication.addComponent(DrugAdapter.fromJson(treatment));
            });
        }

        if (therapyRecommendation.getClinicalTrial() != null && !therapyRecommendation.getClinicalTrial().isEmpty()) {
            therapeuticImplication.getComponent()
                    .addAll(ClinicalTrialAdapter
                            .fromJson(therapyRecommendation.getClinicalTrial()));
        }

        return therapeuticImplication;

    }

    private static Reference getOrCreatePractitioner(Bundle b, String credentials) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(IdType.newRandomUuid());
        practitioner.addIdentifier(new Identifier().setSystem(patientUri).setValue(credentials));
        b.addEntry().setFullUrl(practitioner.getIdElement().getValue()).setResource(practitioner).getRequest()
                .setUrl("Practitioner?identifier=" + patientUri + "|" + credentials)
                .setIfNoneExist("identifier=" + patientUri + "|" + credentials)
                .setMethod(Bundle.HTTPVerb.PUT);

        return new Reference(practitioner);

    }

    public static TherapyRecommendation toJson(IGenericClient client, List<Regex> regex, Observation ob) {
        TherapyRecommendation therapyRecommendation = new TherapyRecommendation()
                .withComment(new ArrayList<>()).withReasoning(new Reasoning()).withClinicalTrial(new ArrayList<>());

        if (ob.hasPerformer()) {
            Bundle b2 = (Bundle) client
                    .search().forResource(Practitioner.class).where(new TokenClientParam("_id")
                            .exactly().code(ob.getPerformerFirstRep().getReference()))
                    .prettyPrint().execute();
            Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();
            therapyRecommendation.setAuthor(author.getIdentifierFirstRep().getValue());
        }

        therapyRecommendation.setId(ob.getIdentifierFirstRep().getValue());

        List<Treatment> treatments = new ArrayList<>();
        therapyRecommendation.setTreatments(treatments);

        List<fhirspark.restmodel.Reference> references = new ArrayList<>();
        ob.getExtensionsByUrl(GenomicsReportingEnum.RELATEDARTIFACT.getSystem()).forEach(relatedArtifact -> {
            if (((RelatedArtifact) relatedArtifact.getValue())
                    .getType() == RelatedArtifactType.CITATION) {
                references.add(new fhirspark.restmodel.Reference()
                        .withPmid(Integer.valueOf(((RelatedArtifact) relatedArtifact.getValue())
                                .getUrl()
                                .replaceFirst(UriEnum.PUBMED_URI.getUri(), "")))
                        .withName(((RelatedArtifact) relatedArtifact.getValue())
                                .getCitation()));
            }
        });

        therapyRecommendation.setReferences(references);

        ob.getComponent().forEach(result -> {
            if (result.getCode().getCodingFirstRep().getCode().equals("93044-6")) {
                String[] evidence = result.getValueCodeableConcept().getCodingFirstRep().getDisplay()
                        .split(" ");
                therapyRecommendation.setEvidenceLevel(evidence[0]);
                if (evidence.length > 1 && !"null".equals(evidence[1])) {
                    therapyRecommendation.setEvidenceLevelExtension(evidence[1]);
                }
                if (evidence.length > 2) {
                    therapyRecommendation.setEvidenceLevelM3Text(
                            String.join(" ", Arrays.asList(evidence).subList(2,
                                    evidence.length))
                                    .replace("(", "").replace(")", ""));
                }
            }
            if (result.getCode().getCodingFirstRep().getCode().equals("51963-7")) {
                therapyRecommendation.getTreatments().add(DrugAdapter.toJson(result));
            }
            if (result.getCode().getCodingFirstRep().getCode().equals("associated-therapy")) {
                therapyRecommendation.setClinicalTrial(ClinicalTrialAdapter.toJson(result));
            }
        });

        therapyRecommendation
                .setReasoning(ReasoningAdapter.toJson(regex, ob.getDerivedFrom(), ob.getHasMember(), client));

        ob.getNote().forEach(note -> therapyRecommendation.getComment().add(note.getText()));

        return therapyRecommendation;
    }

}
