package fhirspark.adapter.clinicaldata;

import fhirspark.restmodel.ClinicalDatum;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * General interface for all adapters of clinical data.
 */
public interface ClinicalDataAdapter {

    Resource process(ClinicalDatum clinicalData);

    Resource process(ClinicalDatum clinicalData, Reference specimen);

}
