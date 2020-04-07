
package fhirspark.restmodel;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "comment",
    "geneticCounselingRecommended",
    "id",
    "rebiopsyRecommended",
    "therapyRecommendations"
})
public class Therapy {

    @JsonProperty("comment")
    private String comment;
    @JsonProperty("geneticCounselingRecommended")
    private Boolean geneticCounselingRecommended;
    @JsonProperty("id")
    private String id;
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

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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
