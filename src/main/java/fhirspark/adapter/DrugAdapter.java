package fhirspark.adapter;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;

import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.Treatment;

public class DrugAdapter {

    OncoKbDrug drugResolver = new OncoKbDrug();

    public MedicationStatement process(Reference patient, Treatment treatment) {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.getMeta()
                .addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medicationstatement");
        medicationStatement.setStatus(MedicationStatementStatus.INTENDED).setSubject(patient);

        String ncitCode = treatment.getNcitCode() != null ? treatment.getNcitCode()
                : drugResolver.resolveDrug(treatment.getName()).getNcitCode();
        medicationStatement.getMedicationCodeableConcept().getCoding()
                .add(new Coding("http://ncithesaurus-stage.nci.nih.gov", ncitCode, treatment.getName()));

        return medicationStatement;
    }

}