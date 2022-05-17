package fhirspark.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.adapter.clinicaldata.GenericAdapter;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.LoincEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.Reasoning;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;
import fhirspark.settings.Regex;

public final class TherapyRecommendationAdapter {

    private static PubmedPublication pubmedResolver = new PubmedPublication();
    private static GeneticAlterationsAdapter geneticAlterationsAdapter = new GeneticAlterationsAdapter();
    private static DrugAdapter drugAdapter = new DrugAdapter();
    private static String therapyRecommendationUri;
    private static String patientUri;

    private TherapyRecommendationAdapter() {
    }

    public static void initialize(String newTherapyRecommendationUri, String newPatientUri) {
        TherapyRecommendationAdapter.therapyRecommendationUri = newTherapyRecommendationUri;
        TherapyRecommendationAdapter.patientUri = newPatientUri;
    }

    public static Observation fromJson(Bundle bundle, List<Regex> regex, DiagnosticReport diagnosticReport,
            Reference fhirPatient, TherapyRecommendation therapyRecommendation) {
        Observation efficacyObservation = new Observation();
        efficacyObservation.setId(IdType.newRandomUuid());
        efficacyObservation.getMeta().addProfile(GenomicsReportingEnum.MEDICATION_EFFICACY.system);
        efficacyObservation.setStatus(ObservationStatus.FINAL);
        efficacyObservation.addCategory().addCoding(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay()));
        efficacyObservation.getValueCodeableConcept()
                .addCoding(LoincEnum.PRESUMED_RESPONSIVE.toCoding());
        efficacyObservation.getCode()
                .addCoding(LoincEnum.GENETIC_VARIATIONS_EFFECT_ON_DRUG_EFFICACY.toCoding());
        ObservationComponentComponent evidenceComponent = efficacyObservation.addComponent();
        evidenceComponent.getCode().addCoding(LoincEnum.LEVEL_OF_EVIDENCE.toCoding());
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
                    String sampleId = RegexAdapter.applyRegexFromCbioportal(regex, clinical.getSampleId());
                    s = SpecimenAdapter.fromJson(fhirPatient, sampleId);
                    bundle.addEntry().setFullUrl(s.getIdElement().getValue()).setResource(s)
                            .getRequest().setUrl("Specimen?identifier=" + SpecimenAdapter.getSpecimenSystem() + "|"
                                    + sampleId)
                            .setIfNoneExist("identifier=" + SpecimenAdapter.getSpecimenSystem() + "|"
                                    + sampleId)
                            .setMethod(Bundle.HTTPVerb.PUT);
                }
                try {
                    Method m = Class.forName("fhirspark.adapter.clinicaldata." + clinical.getAttributeId())
                            .getMethod("process", ClinicalDatum.class);
                    efficacyObservation.addHasMember(new Reference((Resource) m.invoke(null, clinical)));
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                        InvocationTargetException e) {
                    GenericAdapter genericAdapter = new GenericAdapter();
                    efficacyObservation
                            .addHasMember(new Reference(genericAdapter.fromJson(clinical, new Reference(s))));
                }
            });
        }

        if (therapyRecommendation.getReasoning().getGeneticAlterations() != null) {
            therapyRecommendation.getReasoning().getGeneticAlterations().forEach(geneticAlteration -> {
                String uniqueString = "component-value-concept=" + UriEnum.NCBI_GENE.uri + "|"
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
                Extension ex = new Extension().setUrl(GenomicsReportingEnum.RELATEDARTIFACT.system);
                RelatedArtifact relatedArtifact = new RelatedArtifact().setType(RelatedArtifactType.CITATION)
                        .setUrl(UriEnum.PUBMED_URI.uri + reference.getPmid()).setCitation(title);
                ex.setValue(relatedArtifact);
                efficacyObservation.addExtension(ex);
            });
        }

        if (therapyRecommendation.getTreatments() != null) {
            therapyRecommendation.getTreatments().forEach(treatment -> {
                Task medicationChange = new Task().setStatus(TaskStatus.REQUESTED)
                        .setIntent(TaskIntent.PROPOSAL).setFor(fhirPatient);
                medicationChange.setId(IdType.newRandomUuid());
                medicationChange.getMeta().addProfile(GenomicsReportingEnum.MEDICATIONCHANGE.system);

                MedicationStatement ms = drugAdapter.fromJson(fhirPatient, treatment);

                medicationChange.getCode()
                        .addCoding(LoincEnum.CONSIDER_ALTERNATIVE_MEDICATION.toCoding());
                medicationChange.setFocus(new Reference(ms));
                String ncit = ms.getMedicationCodeableConcept().getCodingFirstRep().getCode();
                if (ncit == null) {
                    ncit = treatment.getName();
                }
                medicationChange.addIdentifier(new Identifier().setSystem(UriEnum.NCIT_URI.uri).setValue(ncit));

                Extension ex = new Extension().setUrl(GenomicsReportingEnum.RECOMMENDEDACTION.system);
                ex.setValue(new Reference(medicationChange));
                diagnosticReport.addExtension(ex);

                bundle.addEntry().setFullUrl(medicationChange.getIdElement().getValue())
                        .setResource(medicationChange).getRequest()
                        .setUrl("Task?identifier=" + UriEnum.NCIT_URI.uri + "|" + ncit + "&subject="
                                + fhirPatient.getResource().getIdElement())
                        .setIfNoneExist("identifier=" + UriEnum.NCIT_URI.uri + "|" + ncit + "&subject="
                                + fhirPatient.getResource().getIdElement())
                        .setMethod(Bundle.HTTPVerb.PUT);

                ObservationComponentComponent assessed = efficacyObservation.addComponent();
                assessed.getCode().addCoding(LoincEnum.MEDICATION_ASSESSED.toCoding());
                assessed.setValue(ms.getMedicationCodeableConcept());

            });
        }

        return efficacyObservation;

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

    public static TherapyRecommendation toJson(IGenericClient client, Mtb mtb, List<Regex> regex, Observation ob) {
        TherapyRecommendation therapyRecommendation = new TherapyRecommendation()
                .withComment(new ArrayList<String>()).withReasoning(new Reasoning());
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
            GenericAdapter genericAdapter = new GenericAdapter();
            ClinicalDatum cd = genericAdapter.toJson(regex, (Observation) member.getResource());
            therapyRecommendation.getReasoning().getClinicalData().add(cd);            
        });

        List<Treatment> treatments = new ArrayList<Treatment>();
        therapyRecommendation.setTreatments(treatments);

        List<fhirspark.restmodel.Reference> references = new ArrayList<fhirspark.restmodel.Reference>();
        ob.getExtensionsByUrl(GenomicsReportingEnum.RELATEDARTIFACT.system).forEach(relatedArtifact -> {
            if (((RelatedArtifact) relatedArtifact.getValue())
                    .getType() == RelatedArtifactType.CITATION) {
                references.add(new fhirspark.restmodel.Reference()
                        .withPmid(Integer.valueOf(((RelatedArtifact) relatedArtifact.getValue())
                                .getUrl().replaceFirst(UriEnum.PUBMED_URI.uri, "")))
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
                therapyRecommendation.getTreatments().add(drugAdapter.toJson(result));
            }
        });

        ob.getDerivedFrom().forEach(reference -> {
            geneticAlterations.add(geneticAlterationsAdapter.toJson((Observation) reference.getResource()));
        });

        ob.getNote().forEach(note -> therapyRecommendation.getComment().add(note.getText()));

        return therapyRecommendation;
    }

}
