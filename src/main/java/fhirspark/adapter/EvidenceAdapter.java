package fhirspark.adapter;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Evidence;
import org.hl7.fhir.r4.model.EvidenceVariable;
import org.hl7.fhir.r4.model.Reference;

public class EvidenceAdapter {

    public Evidence process(String evidenceLevel) {

        Evidence evidence = new Evidence();

        evidence.addIdentifier().setSystem("https://cbioportal.org/evidence/BW/").setValue(evidenceLevel.toUpperCase());

        evidence.setName(evidenceLevel);

        EvidenceVariable evidenceVariable = new EvidenceVariable();
        evidenceVariable.addCharacteristic().getDefinitionCodeableConcept()
                .addCoding(new Coding("https://cbioportal.org/evidence/BW/", "Def", "Def"));

        evidence.setExposureBackground(new Reference(evidenceVariable));

        return evidence;
    }

}