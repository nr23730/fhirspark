package fhirspark.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import fhirspark.adapter.clinicaldata.GenericAdapter;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.PubmedPublication;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.restmodel.TherapyRecommendation;
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

    public static Observation fromJson(Bundle bundle, List<Regex> regex, DiagnosticReport diagnosticReport, Reference fhirPatient, TherapyRecommendation therapyRecommendation) {
        Observation efficacyObservation = new Observation();
        efficacyObservation.setId(IdType.newRandomUuid());
        efficacyObservation.getMeta().addProfile(GenomicsReportingEnum.MEDICATION_EFFICACY.system);
        efficacyObservation.setStatus(ObservationStatus.FINAL);
        efficacyObservation.addCategory().addCoding(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay()));
        efficacyObservation.getValueCodeableConcept()
                .addCoding(new Coding(UriEnum.LOINC_URI.uri, "LA9661-5", "Presumed responsive"));
        efficacyObservation.getCode()
                .addCoding(new Coding(UriEnum.LOINC_URI.uri, "51961-1", "Genetic variation's effect on drug efficacy"));
        ObservationComponentComponent evidenceComponent = efficacyObservation.addComponent();
        evidenceComponent.getCode().addCoding(new Coding(UriEnum.LOINC_URI.uri, "93044-6", "Level of evidence"));
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
                            .getRequest().setUrl("Specimen?identifier=https://cbioportal.org/specimen/|"
                                    + sampleId)
                            .setIfNoneExist("identifier=https://cbioportal.org/specimen/|"
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
                        .addCoding(new Coding(UriEnum.LOINC_URI.uri, "LA26421-0", "Consider alternative medication"));
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
                assessed.getCode().addCoding(new Coding(UriEnum.LOINC_URI.uri, "51963-7", "Medication assessed [ID]"));
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

}
