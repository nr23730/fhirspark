package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum ResponseEnum {
    CR("LA28366-5", "Complete response"),
    PR("LA28369-9", "Partial response"),
    SD("LA28371-5", "Stable disease"),
    PD("LA28370-7", "Progressive disease"),
    NA("LA9348-9", "Not assessed");

    private final String code;
    private final String display;

    ResponseEnum(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(UriEnum.LOINC_URI.getUri() + "/88040-1", this.code, this.display);
    }

}
