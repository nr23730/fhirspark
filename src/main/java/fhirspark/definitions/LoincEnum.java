package fhirspark.definitions;

import org.hl7.fhir.r4.model.Coding;

public enum LoincEnum {
    GENOMIC_ALT_ALLELE("69551-0", "Genomic alt allele [ID]"),
    GENOMIC_REF_ALLELE("69547-8", "Genomic ref allele [ID]"),
    CLINICAL_FINDING("75321-0", "Clinical finding"),
    DBSNP("81255-2", "dbSNP [ID]"),
    SAMPLE_VARIANT_ALLELE_FREQUENCY("81258-6", "Sample variant allelic frequency [NFr]"),
    GENE_STUDIED("48018-6", "Gene studied [ID]"),
    GENETIC_VARIANT_ASSESSMENT("69548-6", "Genetic variant assessment"),
    PRESENT("LA9633-4", "Present"),
    CYTOGENETIC_CHROMOSOME_LOCATION("48001-2", "Cytogenetic (chromosome) location"),
    CHROMOSOME_COPY_NUMBER_CHANGE("62378-5", "Chromosome copy number change [Type]"),
    COPY_NUMBER_GAIN("LA14033-7", "Copy number gain"),
    COPY_NUMBER_LOSS("LA14034-5", "Copy number loss"),
    AMINO_ACID_CHANGE("48005-3", "Amino acid change (pHGVS)"),
    DISCRETE_GENETIC_VARIANT("81252-9", "Discrete genetic variant"),
    MASTER_HL7_GENETIC_VARIANT_REPORTING_PANEL("81247-9", "Master HL7 genetic variant reporting panel"),
    GENETIC_COUNSELING_RECOMMENDED("LA14020-4", "Genetic counseling recommended"),
    CONFIRMATORY_TESTING_RECOMMENDED("LA14021-2", "Confirmatory testing recommended"),
    LEVEL_OF_EVIDENCE("93044-6", "Level of evidence"),
    CONSIDER_ALTERNATIVE_MEDICATION("LA26421-0", "Consider alternative medication"),
    MEDICATION_ASSESSED("51963-7", "Medication assessed [ID]"),
    EXACT_START_END("exact-start-end", "Variant exact start and end");

    private final String code;
    private final String display;

    LoincEnum(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public Coding toCoding() {
        return new Coding(UriEnum.LOINC_URI.getUri(), this.code, this.display);
    }

    public static LoincEnum fromCode(String s) {
        for (LoincEnum e : LoincEnum.values()) {
            if (e.code.equals(s)) {
                return e;
            }
        }
        return null;
    }

}
