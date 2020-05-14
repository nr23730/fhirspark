
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entrezGeneId",
    "hugoSymbol",
    "alteration"
})
public class GeneticAlterationsMissing {

    @JsonProperty("entrezGeneId")
    private Integer entrezGeneId;
    @JsonProperty("hugoSymbol")
    private String hugoSymbol;
    @JsonProperty("alteration")
    private String alteration;

    @JsonProperty("entrezGeneId")
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    @JsonProperty("entrezGeneId")
    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public GeneticAlterationsMissing withEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
        return this;
    }

    @JsonProperty("hugoSymbol")
    public String getHugoSymbol() {
        return hugoSymbol;
    }

    @JsonProperty("hugoSymbol")
    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public GeneticAlterationsMissing withHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
        return this;
    }

    @JsonProperty("alteration")
    public String getAlteration() {
        return alteration;
    }

    @JsonProperty("alteration")
    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public GeneticAlterationsMissing withAlteration(String alteration) {
        this.alteration = alteration;
        return this;
    }

}
