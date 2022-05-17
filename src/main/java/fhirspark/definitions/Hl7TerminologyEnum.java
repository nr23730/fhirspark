package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum Hl7TerminologyEnum {
    MR("0203", "MR", "Medical record number"),
    GE("0074", "GE", "Genetic"),
    TUMOR("0487", "TUMOR", "Tumor");

    private final String table;
    private final String code;
    private final String display;

    Hl7TerminologyEnum(String table, String code, String display) {
        this.table = table;
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(UriEnum.HL7_TERMINOLOGY.getUri() + this.table, this.code, this.display);
    }

}
