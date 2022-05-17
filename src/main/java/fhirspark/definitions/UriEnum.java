package fhirspark.definitions;

public enum UriEnum {
    LOINC_URI("http://loinc.org"),
    PUBMED_URI("https://www.ncbi.nlm.nih.gov/pubmed/"),
    NCIT_URI("http://ncithesaurus-stage.nci.nih.gov"),
    HL7_TERMINOLOGY("http://terminology.hl7.org/CodeSystem/v2-"),
    NCBI_GENE("http://www.ncbi.nlm.nih.gov/gene"),
    CLINVAR("http://www.ncbi.nlm.nih.gov/clinvar"),
    COSMIC("http://cancer.sanger.ac.uk/cancergenome/projects/cosmic"),
    GENENAMES("http://www.genenames.org/geneId"),
    UCUM("http://unitsofmeasure.org"),
    DBSNP("http://www.ncbi.nlm.nih.gov/projects/SNP"),
    HGVS("http://varomen.hgvs.org");

    public final String uri;

    private UriEnum(String uri) {
        this.uri = uri;
    }
    
}
