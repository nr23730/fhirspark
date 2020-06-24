package fhirspark.adapter.clinicaldata;

import org.hl7.fhir.r4.model.Resource;

import fhirspark.restmodel.ClinicalDatum;

public interface ClinicalDataAdapter {

    public Resource process(ClinicalDatum clinicalData);

}
