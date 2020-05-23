
package fhirspark.restmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "clinicalData",
    "geneticAlterations"
})
public class Reasoning {

    @JsonProperty("clinicalData")
    private List<ClinicalData> clinicalData = null;
    @JsonProperty("geneticAlterations")
    private List<GeneticAlteration> geneticAlterations = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("clinicalData")
    public List<ClinicalData> getClinicalData() {
        return clinicalData;
    }

    @JsonProperty("clinicalData")
    public void setClinicalData(List<ClinicalData> clinicalData) {
        this.clinicalData = clinicalData;
    }

    public Reasoning withClinicalData(List<ClinicalData> clinicalData) {
        this.clinicalData = clinicalData;
        return this;
    }

    @JsonProperty("geneticAlterations")
    public List<GeneticAlteration> getGeneticAlterations() {
        return geneticAlterations;
    }

    @JsonProperty("geneticAlterations")
    public void setGeneticAlterations(List<GeneticAlteration> geneticAlterations) {
        this.geneticAlterations = geneticAlterations;
    }

    public Reasoning withGeneticAlterations(List<GeneticAlteration> geneticAlterations) {
        this.geneticAlterations = geneticAlterations;
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

    public Reasoning withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
