package fhirspark.adapter;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.MolekulargenetischerBefundberichtEnum;
import fhirspark.restmodel.FollowUp;
import fhirspark.restmodel.ResponseCriteria;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.settings.Regex;
import fhirspark.settings.Settings;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import java.lang.reflect.InvocationTargetException;
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
        if (medicationStatement.getNote().size() > 0) {
            followUp.setComment(medicationStatement.getNote().get(0).getText());
        }


        followUp.setTherapyRecommendationRealized(medicationStatement.getStatus()
            .compareTo(MedicationStatement.MedicationStatementStatus.fromCode("completed")) == 0);
        followUp.setSideEffect(medicationStatement.hasStatusReason());


        ResponseCriteria respCrit = new ResponseCriteria();
        TherapyRecommendation therapyRecommendation = new TherapyRecommendation();

        for (Reference reference : medicationStatement.getReasonReference()) {

            Bundle b1 = (Bundle) client.search().forResource(Observation.class)
                .where(new TokenClientParam("_id")
                .exactly().code(reference.getReference())).prettyPrint()
                .execute();

            Observation obs = (Observation) b1.getEntryFirstRep().getResource();

            if (obs.getIdentifierFirstRep().getValue().startsWith("response_")) {
                String tag = obs.getNoteFirstRep().getText();
                Boolean val = obs.getValueBooleanType().getValue();

                try {
                    ResponseCriteria.class
                        .getDeclaredMethod("set" + tag, Boolean.class)
                        .invoke(respCrit, val);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            } else if (obs.getIdentifierFirstRep().getValue().startsWith(patientId + "_")) {

                therapyRecommendation = TherapyRecommendationAdapter.toJson(client,
                        regex, obs);

            }

        }

        followUp.setTherapyRecommendation(therapyRecommendation);
        followUp.setResponse(respCrit);

        return followUp;
    }

    public static void fromJson(Bundle bundle, List<Regex> regex, Reference fhirPatient,
        String patientId, FollowUp followUp) {

        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.getMeta().addProfile(GenomicsReportingEnum.GENOMICS_REPORT.getSystem());
        medicationStatement.getMeta().addProfile(MolekulargenetischerBefundberichtEnum.GENOMICS_REPORT.getSystem());
        medicationStatement.addIdentifier().setSystem(followUpUri).setValue(followUp.getId());
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

        if (followUp.getSideEffect()) {
            Coding coding = new Coding();
            CodeableConcept cc = new CodeableConcept();
            coding.setSystem("http://hl7.org/fhir/ValueSet/reason-medication-status-codes");
            coding.setCode("395009001");
            coding.setDisplay("Medication stopped - side effect");
            cc.addCoding(coding);
            medicationStatement.addStatusReason(cc);
        }

        if (!followUp.getId().startsWith("followUp_" + patientId + "_")) {
            throw new IllegalArgumentException("Invalid followUp id!");
        }

        //medicationStatement.setIssued(new Date(Long.valueOf(mtb.getId().replace("mtb_" + patientId + "_", ""))));

        if (followUp.getResponse() != null) {
            ResponseCriteria response = followUp.getResponse();
            ArrayList<String> responseTags = new ArrayList<String>();
            ArrayList<Boolean> responseValues = new ArrayList<Boolean>();
            final int respCount = 12;

            responseTags.add("Pd3");
            responseValues.add(response.getPd3());
            responseTags.add("Pr3");
            responseValues.add(response.getPr3());
            responseTags.add("Cr3");
            responseValues.add(response.getCr3());
            responseTags.add("Sd3");
            responseValues.add(response.getSd3());
            responseTags.add("Pd6");
            responseValues.add(response.getPd6());
            responseTags.add("Pr6");
            responseValues.add(response.getPr6());
            responseTags.add("Cr6");
            responseValues.add(response.getCr6());
            responseTags.add("Sd6");
            responseValues.add(response.getSd6());
            responseTags.add("Pd12");
            responseValues.add(response.getPd12());
            responseTags.add("Pr12");
            responseValues.add(response.getPr12());
            responseTags.add("Cr12");
            responseValues.add(response.getCr12());
            responseTags.add("Sd12");
            responseValues.add(response.getSd12());

            for (int i = 0; i < respCount; i++) {

                Observation responseObs = new Observation();

                responseObs.getCode()
                    .addCoding(GenomicsReportingEnum.THERAPEUTIC_IMPLICATION_CODING.toCoding());

                Annotation note = new Annotation();
                note.setText(responseTags.get(i));
                ArrayList<Annotation> notes = new ArrayList<Annotation>();
                notes.add(note);
                responseObs.setNote(notes);

                BooleanType bType = new BooleanType(responseValues.get(i));
                responseObs.setValue(bType);

                DateTimeType dTime = new DateTimeType(followUp.getDate());
                responseObs.setEffective(dTime);

                responseObs.setId(IdType.newRandomUuid());
                responseObs.setStatus(Observation.ObservationStatus.FINAL);
                responseObs.addIdentifier().setSystem("https://cbioportal.org/followUpResponse/")
                    .setValue("response_" + responseTags.get(i) + "_" + followUp.getId());
                bundle.addEntry().setFullUrl(responseObs.getIdElement().getValue()).setResource(responseObs)
                        .getRequest().setUrl(
                            "Observation?identifier=" + "response_" + responseTags.get(i) + "_" + followUp.getId()
                        )
                        .setIfNoneExist("identifier=" + "response_" + responseTags.get(i) + "_" + followUp.getId())
                        .setMethod(Bundle.HTTPVerb.PUT);
                medicationStatement.addReasonReference(new Reference(responseObs)
                    .setDisplay("TherapyResponse" + responseTags.get(i)));
            }
        }

        bundle.addEntry().setFullUrl(medicationStatement.getIdElement().getValue()).setResource(medicationStatement)
                .getRequest().setUrl("MedicationStatement?identifier=" + followUpUri + "|" + followUp.getId())
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

    private static Reference getTherapyRecommendationReference(Bundle b, String credentials, String trIdentifier) {

        Bundle b1 = (Bundle) client.search().forResource(Observation.class)
                .where(new TokenClientParam("identifier")
                .exactly().code(trIdentifier)).prettyPrint()
                .execute();

        Observation obs = (Observation) b1.getEntryFirstRep().getResource();
        String id = obs.getIdElement().getIdPart();

        return new Reference("Observation/" + id).setDisplay("BaseTherapyRecommendation");

    }

}
