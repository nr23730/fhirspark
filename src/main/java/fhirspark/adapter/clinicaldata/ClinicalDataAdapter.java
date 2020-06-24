package fhirspark.adapter.clinicaldata;

import fhirspark.restmodel.ClinicalDatum;
import org.hl7.fhir.r4.model.Resource;

public interface ClinicalDataAdapter {

    public Resource process(ClinicalDatum clinicalData);

}
