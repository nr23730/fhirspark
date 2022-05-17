package fhirspark.adapter;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.Hl7TerminologyEnum;
import fhirspark.definitions.LoincEnum;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.settings.Regex;
import fhirspark.settings.Settings;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.ServiceRequest.ServiceRequestStatus;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public final class MtbAdapter {

    private static IGenericClient client;
    private static String patientUri;
    private static String therapyRecommendationUri;
    private static String mtbUri;
    private static String serviceRequestUri;

    private MtbAdapter() {
    }

    public static void initialize(Settings settings, IGenericClient fhirClient) {
        MtbAdapter.client = fhirClient;
        MtbAdapter.patientUri = settings.getPatientSystem();
        MtbAdapter.therapyRecommendationUri = settings.getObservationSystem();
        MtbAdapter.mtbUri = settings.getDiagnosticReportSystem();
        MtbAdapter.serviceRequestUri = settings.getServiceRequestSystem();
    }

    public static Mtb toJson(List<Regex> regex, String patientId,
            DiagnosticReport diagnosticReport) {
        Mtb mtb = new Mtb().withTherapyRecommendations(new ArrayList<>())
                .withSamples(new ArrayList<>());

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
        mtb.getSamples().addAll(SpecimenAdapter.toJson(regex, diagnosticReport.getSpecimen()));

        for (Reference reference : diagnosticReport.getResult()) {

            List<Extension> recommendedActionReferences = diagnosticReport
                    .getExtensionsByUrl(GenomicsReportingEnum.RECOMMENDEDACTION.system);

            recommendedActionReferences.forEach(recommendedActionReference -> {

                Task t = (Task) ((Reference) recommendedActionReference.getValue()).getResource();
                if (t != null) {
                    assert t.getMeta().getProfile().get(0).getValue()
                            .equals(GenomicsReportingEnum.TASK_REC_FOLLOWUP.system);
                    Coding c = t.getCode().getCodingFirstRep();
                    switch (LoincEnum.fromCode(c.getCode())) {
                        case CONFIRMATORY_TESTING_RECOMMENDED:
                            mtb.setRebiopsyRecommendation(true);
                            break;
                        case GENETIC_COUNSELING_RECOMMENDED:
                            mtb.setGeneticCounselingRecommendation(true);
                            break;
                        default:
                            break;
                    }
                }
            });

            switch (GenomicsReportingEnum
                    .fromSystem(reference.getResource().getMeta().getProfile().get(0).getValue())) {
                case MEDICATION_EFFICACY:
                    TherapyRecommendation therapyRecommendation = TherapyRecommendationAdapter.toJson(client,
                            regex, (Observation) reference.getResource());
                    mtb.getTherapyRecommendations().add(therapyRecommendation);
                    break;
                default:
                    break;
            }
        }

        return mtb;

    }

    public static void fromJson(Bundle bundle, List<Regex> regex, Reference fhirPatient, String patientId, Mtb mtb) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.getMeta().addProfile(GenomicsReportingEnum.GENOMICS_REPORT.system);
        diagnosticReport.setId(IdType.newRandomUuid());
        diagnosticReport.setSubject(fhirPatient);
        diagnosticReport.addCategory().addCoding(Hl7TerminologyEnum.GE.toCoding());
        diagnosticReport.getCode()
                .addCoding(LoincEnum.MASTER_HL7_GENETIC_VARIANT_REPORTING_PANEL.toCoding());

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
            t.getMeta().addProfile(GenomicsReportingEnum.TASK_REC_FOLLOWUP.system);
            t.setFor(fhirPatient);
            t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
            t.getCode().setText("Recommended follow-up")
                    .addCoding(LoincEnum.GENETIC_COUNSELING_RECOMMENDED.toCoding());
            Extension ex = new Extension().setUrl(GenomicsReportingEnum.RECOMMENDEDACTION.system);
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
            t.getMeta().addProfile(GenomicsReportingEnum.TASK_REC_FOLLOWUP.system);
            t.setFor(fhirPatient);
            t.setStatus(TaskStatus.REQUESTED).setIntent(TaskIntent.PROPOSAL);
            t.getCode().setText("Recommended follow-up")
                    .addCoding(LoincEnum.CONFIRMATORY_TESTING_RECOMMENDED.toCoding());
            Extension ex = new Extension().setUrl(GenomicsReportingEnum.RECOMMENDEDACTION.system);
            ex.setValue(new Reference(t));
            diagnosticReport.addExtension(ex);
        }

        mtb.getSamples().forEach(sample -> {
            String sampleId = RegexAdapter.applyRegexFromCbioportal(regex, sample);
            Specimen s = SpecimenAdapter.fromJson(fhirPatient, sampleId);
            bundle.addEntry().setFullUrl(s.getIdElement().getValue()).setResource(s)
                    .getRequest().setUrl("Specimen?identifier=" + SpecimenAdapter.getSpecimenSystem() + "|" + sampleId)
                    .setIfNoneExist("identifier=" + SpecimenAdapter.getSpecimenSystem() + "|" + sampleId)
                    .setMethod(Bundle.HTTPVerb.PUT);
            diagnosticReport.addSpecimen(new Reference(s));
        });

        for (TherapyRecommendation therapyRecommendation : mtb.getTherapyRecommendations()) {
            Observation efficacyObservation = TherapyRecommendationAdapter.fromJson(bundle, regex, diagnosticReport,
                    fhirPatient, therapyRecommendation);
            bundle.addEntry().setFullUrl(efficacyObservation.getIdElement().getValue())
                    .setResource(efficacyObservation).getRequest()
                    .setUrl("Observation?identifier=" + therapyRecommendationUri + "|"
                            + therapyRecommendation.getId())
                    .setIfNoneExist("identifier=" + therapyRecommendationUri + "|" + therapyRecommendation.getId())
                    .setMethod(Bundle.HTTPVerb.PUT);
            diagnosticReport.addResult(new Reference(efficacyObservation));
        }

        bundle.addEntry().setFullUrl(diagnosticReport.getIdElement().getValue()).setResource(diagnosticReport)
                .getRequest().setUrl("DiagnosticReport?identifier=" + mtbUri + "|" + mtb.getId())
                .setIfNoneExist("identifier=" + mtbUri + "|" + mtb.getId()).setMethod(Bundle.HTTPVerb.PUT);

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
