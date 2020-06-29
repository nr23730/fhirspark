package fhirspark.resolver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "HGNC ID", "Approved symbol", "NCBI Gene ID"})
public class Genenames {

    @JsonProperty("HGNC ID")
    private String hgncId;
    @JsonProperty("Approved symbol")
    private String approvedSymbol;
    @JsonProperty("NCBI Gene ID")
    private Integer ncbiGeneId;

    @JsonProperty("HGNC ID")
    public String getHgncId() {
        return hgncId;
    }

    @JsonProperty("HGNC ID")
    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    @JsonProperty("Approved symbol")
    public String getApprovedSymbol() {
        return approvedSymbol;
    }

    @JsonProperty("Approved symbol")
    public void setApprovedSymbol(String approvedSymbol) {
        this.approvedSymbol = approvedSymbol;
    }

    @JsonProperty("NCBI Gene ID")
    public Integer getNcbiGeneId() {
        return ncbiGeneId;
    }

    @JsonProperty("NCBI Gene ID")
    public void setNcbiGeneId(Integer ncbiGeneId) {
        this.ncbiGeneId = ncbiGeneId;
    }

}