package fhirspark.clinicaldata;

import org.hl7.fhir.r4.model.Resource;

import fhirspark.restmodel.ClinicalData;

public interface ClinicalDataAdapter {

    public Resource process(ClinicalData clinicalData);

}