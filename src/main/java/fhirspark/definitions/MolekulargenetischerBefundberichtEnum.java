package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum MolekulargenetischerBefundberichtEnum {
    THERAPEUTIC_IMPLICATION(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-molgen/StructureDefinition/therapeutische-implikation",
            null, null),
    GENOMICS_REPORT(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-molgen/StructureDefinition/molekulargenetischer-befundbericht",
            null, null),
    VARIANT("https://www.medizininformatik-initiative.de/fhir/ext/modul-molgen/StructureDefinition/variante", null,
            null),
    TASK_REC_FOLLOWUP(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-molgen/StructureDefinition/empfohlene-folgemassnahme",
            null, null);

    private final String system;
    private final String code;
    private final String display;

    MolekulargenetischerBefundberichtEnum(String system, String code, String display) {
        this.system = system;
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(this.system, this.code, this.display);
    }

    public static MolekulargenetischerBefundberichtEnum fromSystem(String s) {
        for (MolekulargenetischerBefundberichtEnum e : MolekulargenetischerBefundberichtEnum.values()) {
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
