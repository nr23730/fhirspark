
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hugoSymbol",
    "entrezGeneId",
    "proteinChange"
})
public class GeneticAlteration {

    @JsonProperty("hugoSymbol")
    private String hugoSymbol;
    @JsonProperty("entrezGeneId")
    private Integer entrezGeneId;
    @JsonProperty("proteinChange")
    private String proteinChange;

    @JsonProperty("hugoSymbol")
    public String getHugoSymbol() {
        return hugoSymbol;
    }

    @JsonProperty("hugoSymbol")
    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    @JsonProperty("entrezGeneId")
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    @JsonProperty("entrezGeneId")
    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    @JsonProperty("proteinChange")
    public String getProteinChange() {
        return proteinChange;
    }

    @JsonProperty("proteinChange")
    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

}
