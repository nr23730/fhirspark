package fhirspark.adapter.clinicaldata;

import fhirspark.restmodel.ClinicalDatum;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * Generic adapter for clinical data. Also fallback if other adapter was not found.
 */
public class GenericAdapter implements ClinicalDataAdapter {

    @Override
    public Resource fromJson(ClinicalDatum clinicalData) {
        Observation obs = new Observation();
        obs.setStatus(ObservationStatus.UNKNOWN);
        obs.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "75321-0", "Clinical finding")));

        obs.getValueStringType().setValue(clinicalData.getAttributeName() + ": " + clinicalData.getValue());

        return obs;
    }

    @Override
    public Resource fromJson(ClinicalDatum clinicalData, Reference specimen) {
        Observation obs = (Observation) fromJson(clinicalData);
        obs.setSpecimen(specimen);

        return obs;
    }

    @Override
    public ClinicalDatum toJson(Observation fhirResource) {
        return null;
    }

}
