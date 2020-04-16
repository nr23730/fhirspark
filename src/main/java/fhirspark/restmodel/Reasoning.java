
package fhirspark.restmodel;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "geneticAlterations",
    "geneticAlterationsMissing",
    "clinicalData"
})
public class Reasoning {

    @JsonProperty("geneticAlterations")
    private List<GeneticAlteration> geneticAlterations = null;
    @JsonProperty("geneticAlterationsMissing")
    private List<GeneticAlterationsMissing> geneticAlterationsMissing = null;
    @JsonProperty("clinicalData")
    private List<ClinicalData> clinicalData = null;

    @JsonProperty("geneticAlterations")
    public List<GeneticAlteration> getGeneticAlterations() {
        return geneticAlterations;
    }

    @JsonProperty("geneticAlterations")
    public void setGeneticAlterations(List<GeneticAlteration> geneticAlterations) {
        this.geneticAlterations = geneticAlterations;
    }

    @JsonProperty("geneticAlterationsMissing")
    public List<GeneticAlterationsMissing> getGeneticAlterationsMissing() {
        return geneticAlterationsMissing;
    }

    @JsonProperty("geneticAlterationsMissing")
    public void setGeneticAlterationsMissing(List<GeneticAlterationsMissing> geneticAlterationsMissing) {
        this.geneticAlterationsMissing = geneticAlterationsMissing;
    }

    @JsonProperty("clinicalData")
    public List<ClinicalData> getClinicalData() {
        return clinicalData;
    }

    @JsonProperty("clinicalData")
    public void setClinicalData(List<ClinicalData> clinicalData) {
        this.clinicalData = clinicalData;
    }

}
