package fhirspark.adapter.clinicaldata;

import fhirspark.restmodel.ClinicalDatum;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * General interface for all adapters of clinical data.
 */
public interface ClinicalDataAdapter {

    Resource fromJson(ClinicalDatum clinicalData);

    Resource fromJson(ClinicalDatum clinicalData, Reference specimen);

    ClinicalDatum toJson(Observation fhirResource);

}
