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
    DISCRETE_GENETIC_VARIANT("81252-9", "Discrete genetic variant")
    ;

    public final String code;
    public final String display;

    private LoincEnum(String code, String display) {
        this.code = code;
        this.display = display;
    }
    
    public Coding toCoding() {
        return new Coding(UriEnum.LOINC_URI.uri, this.code, this.display);
    }

}
