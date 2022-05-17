package fhirspark.adapter;

import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.Treatment;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Reference;

/**
 * Adapter that processes Drugs to a HL7 FHIR MedicationStatement.
 */
public class DrugAdapter {

    /**
     *
     * @param patient   Reference to the patient is medication belongs to.
     * @param treatment The treatment that the patient should receive.
     * @return Composed MedicationStatement resource.
     */
    public MedicationStatement fromJson(Reference patient, Treatment treatment) {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.getMeta()
                .addProfile(GenomicsReportingEnum.MEDICATIONSTATEMENT.system);
        medicationStatement.setStatus(MedicationStatementStatus.UNKNOWN).setSubject(patient);

        String ncitCode = treatment.getNcitCode() != null ? treatment.getNcitCode()
                : OncoKbDrug.resolve(treatment.getName()).getNcitCode();
        if (ncitCode != null) {
            medicationStatement.getMedicationCodeableConcept().getCoding()
                    .add(new Coding(UriEnum.NCIT_URI.uri, ncitCode, treatment.getName()));
        } else {
            medicationStatement.getMedicationCodeableConcept().getCoding()
                    .add(new Coding(null, null, treatment.getName()));
        }

        return medicationStatement;
    }

    public Treatment toJson(ObservationComponentComponent result) {
        return new Treatment()
                .withNcitCode(result.getValueCodeableConcept().getCodingFirstRep().getCode())
                .withName(result.getValueCodeableConcept().getCodingFirstRep().getDisplay());
    }

}
