package fhirspark.adapter;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;

public class SpecimenAdapter {
    
    public Specimen process(Reference patient, String specimen) {
        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setSubject(patient);
        fhirSpecimen.addIdentifier(new Identifier().setSystem("https://cbioportal.org/specimen/").setValue(specimen));

        return fhirSpecimen;
    }

}