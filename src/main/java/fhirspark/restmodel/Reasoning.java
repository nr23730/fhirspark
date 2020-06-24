
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "clinicalData",
    "geneticAlterations"
})
public class Reasoning {

    @JsonProperty("clinicalData")
    private List<ClinicalDatum> clinicalData;
    @JsonProperty("geneticAlterations")
    private List<GeneticAlteration> geneticAlterations;

    @JsonProperty("clinicalData")
    public List<ClinicalDatum> getClinicalData() {
        return clinicalData;
    }

    @JsonProperty("clinicalData")
    public void setClinicalData(List<ClinicalDatum> clinicalData) {
        this.clinicalData = clinicalData;
    }

    public Reasoning withClinicalData(List<ClinicalDatum> clinicalData) {
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

}
