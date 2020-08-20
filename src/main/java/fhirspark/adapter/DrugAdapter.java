package fhirspark.adapter;

import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.Treatment;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;
import org.hl7.fhir.r4.model.Reference;

/**
 * Adapter that processes Drugs to a HL7 FHIR MedicationStatement.
 */
public class DrugAdapter {

    /**
     *
     * @param patient Reference to the patient is medication belongs to.
     * @param treatment The treatment that the patient should receive.
     * @return Composed MedicationStatement resource.
     */
    public MedicationStatement process(Reference patient, Treatment treatment) {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.getMeta()
                .addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medicationstatement");
        medicationStatement.setStatus(MedicationStatementStatus.UNKNOWN).setSubject(patient);

        String ncitCode = treatment.getNcitCode() != null ? treatment.getNcitCode()
                : OncoKbDrug.resolve(treatment.getName()).getNcitCode();
        medicationStatement.getMedicationCodeableConcept().getCoding()
                .add(new Coding("http://ncithesaurus-stage.nci.nih.gov", ncitCode, treatment.getName()));

        return medicationStatement;
    }

}
