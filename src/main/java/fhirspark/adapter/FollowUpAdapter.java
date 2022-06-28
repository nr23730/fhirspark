package fhirspark.adapter;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.MolekulargenetischerBefundberichtEnum;
import fhirspark.restmodel.FollowUp;
import fhirspark.restmodel.ResponseCriteria;
import fhirspark.settings.Regex;
import fhirspark.settings.Settings;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public final class FollowUpAdapter {

    private static IGenericClient client;
    private static String patientUri;
    private static String therapyRecommendationUri;
    private static String mtbUri;
    private static String followUpUri;
    private static String serviceRequestUri;

    private FollowUpAdapter() {
    }

    public static void initialize(Settings settings, IGenericClient fhirClient) {
        FollowUpAdapter.client = fhirClient;
        FollowUpAdapter.patientUri = settings.getPatientSystem();
        FollowUpAdapter.therapyRecommendationUri = settings.getObservationSystem();
        FollowUpAdapter.followUpUri = settings.getFollowUpSystem();
        FollowUpAdapter.mtbUri = settings.getDiagnosticReportSystem();
        FollowUpAdapter.serviceRequestUri = settings.getServiceRequestSystem();
        FollowUpAdapter.serviceRequestUri = settings.getFollowUpSystem();
    }

    public static FollowUp toJson(List<Regex> regex, String patientId, MedicationStatement medicationStatement) {
        FollowUp followUp = new FollowUp();

        if (medicationStatement.hasInformationSource()) {
            Bundle b2 = (Bundle) client.search().forResource(Practitioner.class).where(new TokenClientParam("_id")
                    .exactly().code(medicationStatement.getInformationSource().getReference())).prettyPrint()
                    .execute();
            Practitioner author = (Practitioner) b2.getEntryFirstRep().getResource();
            followUp.setAuthor(author.getIdentifierFirstRep().getValue());
        }

        //followUp.setTherapyRecommendation(medicationStatement.getBasedOnFirstRep().getReference());

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        followUp.setDate(f.format(medicationStatement.getEffectiveDateTimeType().toCalendar().getTime()));

        followUp.setId("followUp_" + patientId + "_"
            + medicationStatement.getEffectiveDateTimeType()
            .toCalendar()
            .getTimeInMillis()
        );
        followUp.setComment(medicationStatement.getNote().get(0).getText());

        followUp.setTherapyRecommendationRealized(medicationStatement.getStatus().getDisplay().equals("COMPLETED"));
        followUp.setSideEffect(medicationStatement.getStatusReasonFirstRep().getText().equals(""));

        return followUp;
    }

    public static void fromJson(Bundle bundle, List<Regex> regex, Reference fhirPatient,
        String patientId, FollowUp followUp) {

        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.getMeta().addProfile(GenomicsReportingEnum.GENOMICS_REPORT.getSystem());
        medicationStatement.getMeta().addProfile(MolekulargenetischerBefundberichtEnum.GENOMICS_REPORT.getSystem());
        medicationStatement.setId(IdType.newRandomUuid());
        medicationStatement.setSubject(fhirPatient);

        medicationStatement.setInformationSource(getOrCreatePractitioner(bundle, followUp.getAuthor()));

        medicationStatement.addReasonReference(
            getTherapyRecommendationReference(bundle, followUp.getAuthor(), followUp.getTherapyRecommendation().getId())
        );

        medicationStatement.getEffectiveDateTimeType().fromStringValue(followUp.getDate());

        medicationStatement.addIdentifier().setValue(followUp.getId());

        if (followUp.getComment() != null) {
            ArrayList<Annotation> annots = new ArrayList<Annotation>();
            Annotation comment = new Annotation();
            comment.setText(followUp.getComment());
            annots.add(comment);
            medicationStatement.setNote(annots);
        }

        if (followUp.getTherapyRecommendationRealized()) {
            medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.fromCode("completed"));
        } else {
            medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.fromCode("not-taken"));
        }

        if (!followUp.getId().startsWith("followup_" + patientId + "_")) {
            throw new IllegalArgumentException("Invalid followUp id!");
        }

        //medicationStatement.setIssued(new Date(Long.valueOf(mtb.getId().replace("mtb_" + patientId + "_", ""))));

        if (followUp.getResponse() != null) {
            ResponseCriteria response = followUp.getResponse();
            Observation responseObs = new Observation();
            responseObs.getCode()
                .addCoding(GenomicsReportingEnum.THERAPEUTIC_IMPLICATION_CODING.toCoding());
            Annotation note = new Annotation();
            note.setText(response.getPd3().toString());
            ArrayList<Annotation> notes = new ArrayList<Annotation>();
            notes.add(note);
            responseObs.setNote(notes);
            responseObs.setId(IdType.newRandomUuid());
            responseObs.setStatus(Observation.ObservationStatus.FINAL);
            bundle.addEntry().setFullUrl(responseObs.getIdElement().getValue()).setResource(responseObs)
                    .getRequest().setUrl("Observation?identifier=" + "response_" + followUp.getId())
                    .setIfNoneExist("identifier=" + "response_" + followUp.getId())
                    .setMethod(Bundle.HTTPVerb.PUT);
            medicationStatement.addReasonReference(new Reference(responseObs));
        }

        bundle.addEntry().setFullUrl(medicationStatement.getIdElement().getValue()).setResource(medicationStatement)
                .getRequest().setUrl("medicationStatement?identifier=" + followUpUri + "|" + followUp.getId())
                .setIfNoneExist("identifier=" + followUpUri + "|" + followUp.getId()).setMethod(Bundle.HTTPVerb.PUT);

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

    private static Reference getTherapyRecommendationReference(Bundle b, String credentials, String mtbIdentifier) {

        DiagnosticReport therapyRecommendation = new DiagnosticReport();
        therapyRecommendation.setId(IdType.newRandomUuid());
        therapyRecommendation.addIdentifier(new Identifier().setSystem(patientUri).setValue(credentials));
        b.addEntry().setFullUrl(therapyRecommendation.getIdElement().getValue())
            .setResource(therapyRecommendation)
            .getRequest()
            .setUrl("DiagnosticReport?identifier=" + mtbIdentifier + "|" + credentials)
            .setIfNoneExist("identifier=" + mtbIdentifier + "|" + credentials).setMethod(Bundle.HTTPVerb.PUT);

        return new Reference(therapyRecommendation);

    }

}
