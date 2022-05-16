package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum LoincEnum {
    GENOMIC_ALT_ALLELE("69551-0", "Genomic alt allele [ID]"),
    GENOMIC_REF_ALLELE("69547-8", "Genomic ref allele [ID]"),
    CLINICAL_FINDING("75321-0", "Clinical finding"),
    ;

    public final String code;
    public final String display;

    private LoincEnum(String code, String display) {
        this.code = code;
        this.display = display;
    }
    
    public Coding toCoding() {
        return new Coding("http://loinc.org", this.code, this.display);
    }

}
