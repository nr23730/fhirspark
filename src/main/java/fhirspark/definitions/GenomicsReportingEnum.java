package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum GenomicsReportingEnum {
    THERAPEUTIC_IMPLICATION(
            "http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/therapeutic-implication", null, null),
    MEDICATION_EFFICACY("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medication-efficacy", null, null),
    SPECIMEN("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/specimen", null, null),
    GENOMICS_REPORT("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/genomics-report", null, null),
    RECOMMENDEDACTION("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/RecommendedAction", null, null),
    VARIANT("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/variant", null, null),
    MEDICATIONSTATEMENT("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/medicationstatement", null,
            null),
    RELATEDARTIFACT("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/CGRelatedArtifact", null, null),
    THERAPEUTIC_IMPLICATION_CODING(
            "http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/TbdCodes", "therapeutic-implication",
            "Therapeutic Implication"),
    EXACT_START_END("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/TbdCodes", "exact-start-end",
            "Variant exact start and end"),
    TASK_REC_FOLLOWUP("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/task-rec-followup", null, null),
    PREDICTED_THERAPEUTIC_IMPLICATION("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/tbd-codes-cs",
            "predicted-therapeutic-implication", "Predicted Therapeutic Implication"),
    ASSOCIATED_THERAPY("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/TbdCodes", "associated-therapy",
            "Genomically linked therapy");

    private final String system;
    private final String code;
    private final String display;

    GenomicsReportingEnum(String system, String code, String display) {
        this.system = system;
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(this.system, this.code, this.display);
    }

    public static GenomicsReportingEnum fromSystem(String s) {
        for (GenomicsReportingEnum e : GenomicsReportingEnum.values()) {
            if (e.system.equals(s)) {
                return e;
            }
        }
        return null;
    }

    /**
     * @return the system
     */
    public String getSystem() {
        return system;
    }

}
