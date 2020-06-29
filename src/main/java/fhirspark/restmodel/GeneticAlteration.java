
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "alteration",
    "entrezGeneId",
    "hugoSymbol",
    "chromosome"
})
public class GeneticAlteration {

    @JsonProperty("alteration")
    private String alteration;
    @JsonProperty("entrezGeneId")
    private Integer entrezGeneId;
    @JsonProperty("hugoSymbol")
    private String hugoSymbol;
    @JsonProperty("chromosome")
    private String chromosome;

    @JsonProperty("alteration")
    public String getAlteration() {
        return alteration;
    }

    @JsonProperty("alteration")
    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public GeneticAlteration withAlteration(String alteration) {
        this.alteration = alteration;
        return this;
    }

    @JsonProperty("entrezGeneId")
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    @JsonProperty("entrezGeneId")
    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public GeneticAlteration withEntrezGeneId(Integer entrezGeneId) {
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

    public GeneticAlteration withHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
        return this;
    }

    @JsonProperty("chromosome")
    public String getChromosome() {
        return chromosome;
    }

    @JsonProperty("chromosome")
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public GeneticAlteration withChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

}
