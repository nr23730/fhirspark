package fhirspark.definitions;

public enum UriEnum {
    LOINC_URI("http://loinc.org"),
    PUBMED_URI("https://www.ncbi.nlm.nih.gov/pubmed/"),
    NCIT_URI("http://ncithesaurus-stage.nci.nih.gov");
    ;

    public final String uri;

    private UriEnum(String uri) {
        this.uri = uri;
    }
    
}
