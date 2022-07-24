package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum SnomedEnum {
    PATIENT_ELIGIBLE_FOR_CLINICAL_TRIAL("399223003", "Patient eligible for clinical trial"),
    SIDE_EFFECT("395009001", "Medication stopped - side effect");

    private final String code;
    private final String display;

    SnomedEnum(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(UriEnum.SNOMED.getUri(), this.code, this.display);
    }

    public static SnomedEnum fromCode(String s) {
        for (SnomedEnum e : SnomedEnum.values()) {
            if (e.code.equals(s)) {
                return e;
            }
        }
        return null;
    }

}
