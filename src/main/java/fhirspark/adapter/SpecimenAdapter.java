package fhirspark.adapter;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;

import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.Hl7TerminologyEnum;

/**
 * Builds a HL7 FHIR Speciment object with the provided specimen id.
 */
public final class SpecimenAdapter {

    private static String specimenSystem;

    public static void initialize(String newSpecimenSystem) {
        SpecimenAdapter.specimenSystem = newSpecimenSystem;
    }

    public static String getSpecimenSystem() {
        return SpecimenAdapter.specimenSystem;
    }

    /**
     *
     * @param patient Reference to the patient is medication belongs to.
     * @param specimen id of the provided specimen.
     * @return HL7 FHIR Specimen object.
     */
    public static Specimen fromJson(Reference patient, String specimen) {
        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setId(IdType.newRandomUuid());
        fhirSpecimen.getMeta().addProfile(GenomicsReportingEnum.SPECIMEN.system);
        fhirSpecimen.setSubject(patient);
        fhirSpecimen.addIdentifier(new Identifier().setSystem(specimenSystem).setValue(specimen));
        fhirSpecimen.getType().addCoding(Hl7TerminologyEnum.TUMOR.toCoding());

        return fhirSpecimen;
    }

}
