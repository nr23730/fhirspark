package fhirspark.adapter.clinicaldata;

import fhirspark.adapter.SpecimenAdapter;
import fhirspark.definitions.LoincEnum;
import fhirspark.restmodel.ClinicalDatum;
import fhirspark.settings.Regex;

import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
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
        obs.setCode(new CodeableConcept().addCoding(LoincEnum.CLINICAL_FINDING.toCoding()));

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
    public ClinicalDatum toJson(List<Regex> regex, Observation obs) {
        String[] attr = obs.getValueStringType().asStringValue().split(": ");
        ClinicalDatum cd = new ClinicalDatum().withAttributeName(attr[0]).withValue(attr[1]);
        if (obs.getSpecimen().getResource() != null) {
            cd.setSampleId(SpecimenAdapter.toJson(regex, obs.getSpecimen()));
        }
        return cd;
    }

}
