package fhirspark.adapter;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;

public class SpecimenAdapter {

    public Specimen process(Reference patient, String specimen) {
        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.getMeta().addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/specimen");
        fhirSpecimen.setSubject(patient);
        fhirSpecimen.addIdentifier(new Identifier().setSystem("https://cbioportal.org/specimen/").setValue(specimen));
        fhirSpecimen.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0487").setCode("TUMOR")
                .setDisplay("Tumor");

        return fhirSpecimen;
    }

}