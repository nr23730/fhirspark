
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mtb",
    "therapyRecommendation"
})
public class Deletions {

    @JsonProperty("mtb")
    private List<String> mtb;
    @JsonProperty("therapyRecommendation")
    private List<String> therapyRecommendation;

    @JsonProperty("mtb")
    public List<String> getMtb() {
        return mtb;
    }

    @JsonProperty("mtb")
    public void setMtb(List<String> mtb) {
        this.mtb = mtb;
    }

    @JsonProperty("therapyRecommendation")
    public List<String> getTherapyRecommendation() {
        return therapyRecommendation;
    }

    @JsonProperty("therapyRecommendation")
    public void setTherapyRecommendation(List<String> therapyRecommendation) {
        this.therapyRecommendation = therapyRecommendation;
    }
    
}