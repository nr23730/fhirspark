
package fhirspark.restmodel;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hugoSymbol",
    "alteration",
    "entrezGeneId"
})
public class GeneticAlteration {

    @JsonProperty("hugoSymbol")
    private String hugoSymbol;
    @JsonProperty("alteration")
    private String alteration;
    @JsonProperty("entrezGeneId")
    private Integer entrezGeneId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public GeneticAlteration withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
