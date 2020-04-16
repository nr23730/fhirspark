
package fhirspark.restmodel;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "comment",
    "geneticCounselingRecommended",
    "rebiopsyRecommended",
    "therapyRecommendations"
})
public class CBioPortalPatient {

    @JsonProperty("comment")
    private String comment;
    @JsonProperty("geneticCounselingRecommended")
    private Boolean geneticCounselingRecommended;
    @JsonProperty("rebiopsyRecommended")
    private Boolean rebiopsyRecommended;
    @JsonProperty("therapyRecommendations")
    private List<TherapyRecommendation> therapyRecommendations = null;

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("geneticCounselingRecommended")
    public Boolean getGeneticCounselingRecommended() {
        return geneticCounselingRecommended;
    }

    @JsonProperty("geneticCounselingRecommended")
    public void setGeneticCounselingRecommended(Boolean geneticCounselingRecommended) {
        this.geneticCounselingRecommended = geneticCounselingRecommended;
    }

    @JsonProperty("rebiopsyRecommended")
    public Boolean getRebiopsyRecommended() {
        return rebiopsyRecommended;
    }

    @JsonProperty("rebiopsyRecommended")
    public void setRebiopsyRecommended(Boolean rebiopsyRecommended) {
        this.rebiopsyRecommended = rebiopsyRecommended;
    }

    @JsonProperty("therapyRecommendations")
    public List<TherapyRecommendation> getTherapyRecommendations() {
        return therapyRecommendations;
    }

    @JsonProperty("therapyRecommendations")
    public void setTherapyRecommendations(List<TherapyRecommendation> therapyRecommendations) {
        this.therapyRecommendations = therapyRecommendations;
    }

}
