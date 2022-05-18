package fhirspark.adapter;

import fhirspark.definitions.LoincEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.Treatment;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;

/**
 * Adapter that processes Drugs to a HL7 FHIR MedicationStatement.
 */
public final class DrugAdapter {

    private DrugAdapter() {
    }

    /**
     *
     * @param treatment The treatment that the patient should receive.
     * @return Observation component for medication.
     */
    public static ObservationComponentComponent fromJson(Treatment treatment) {
        ObservationComponentComponent assessed = new ObservationComponentComponent();
        assessed.getCode().addCoding(LoincEnum.MEDICATION_ASSESSED.toCoding());

        String ncitCode = treatment.getNcitCode() != null ? treatment.getNcitCode()
                : OncoKbDrug.resolve(treatment.getName()).getNcitCode();
        if (ncitCode != null) {
            assessed.getValueCodeableConcept()
                    .addCoding(new Coding(UriEnum.NCIT_URI.getUri(), ncitCode, treatment.getName()));
        } else {
            assessed.getValueCodeableConcept().addCoding(new Coding(null, null, treatment.getName()));
        }

        return assessed;
    }

    public static Treatment toJson(ObservationComponentComponent result) {
        return new Treatment()
                .withNcitCode(result.getValueCodeableConcept().getCodingFirstRep().getCode())
                .withName(result.getValueCodeableConcept().getCodingFirstRep().getDisplay());
    }

}
