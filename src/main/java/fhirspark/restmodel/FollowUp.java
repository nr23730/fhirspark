
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "therapyRecommendation",
    "date",
    "author",
    "comment",
    "therapyRecommendationRealized",
    "sideEffect",
    "response"
})
public class FollowUp {

    @JsonProperty("id")
    private String id;
    @JsonProperty("therapyRecommendation")
    private TherapyRecommendation therapyRecommendation;
    @JsonProperty("date")
    private String date;
    @JsonProperty("author")
    private String author;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("therapyRecommendationRealized")
    private Boolean therapyRecommendationRealized;
    @JsonProperty("sideEffect")
    private Boolean sideEffect;    
    @JsonProperty("response")
    private ResponseCriteria response;

    @JsonProperty("id")
    public String getId() {
        return id;
    }
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    public FollowUp withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("therapyRecommendation")
    public TherapyRecommendation getTherapyRecommendation() {
        return therapyRecommendation;
    }
    @JsonProperty("therapyRecommendation")
    public void setTherapyRecommendation(TherapyRecommendation therapyRecommendation) {
        this.therapyRecommendation = therapyRecommendation;
    }
    public FollowUp withTherapyRecommendation(TherapyRecommendation therapyRecommendation) {
        this.therapyRecommendation = therapyRecommendation;
        return this;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }
    public FollowUp withDate(String date) {
        this.date = date;
        return this;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }
    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }
    public FollowUp withAuthor(String author) {
        this.author = author;
        return this;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }
    public FollowUp withComment(String comment) {
        this.comment = comment;
        return this;
    }

    @JsonProperty("therapyRecommendationRealized")
    public Boolean getTherapyRecommendationRealized() {
        return therapyRecommendationRealized;
    }
    @JsonProperty("therapyRecommendationRealized")
    public void setTherapyRecommendationRealized(Boolean therapyRecommendationRealized) {
        this.therapyRecommendationRealized = therapyRecommendationRealized;
    }
    public FollowUp withTherapyRecommendationRealized(Boolean therapyRecommendationRealized) {
        this.therapyRecommendationRealized = therapyRecommendationRealized;
        return this;
    }

    @JsonProperty("sideEffect")
    public Boolean getSideEffect() {
        return sideEffect;
    }
    @JsonProperty("sideEffect")
    public void setSideEffect(Boolean sideEffect) {
        this.sideEffect = sideEffect;
    }
    public FollowUp withSideEffect(Boolean sideEffect) {
        this.sideEffect = sideEffect;
        return this;
    }

    @JsonProperty("response")
    public ResponseCriteria getResponse() {
        return response;
    }
    @JsonProperty("response")
    public void setResponse(ResponseCriteria response) {
        this.response = response;
    }
    public FollowUp withResponse(ResponseCriteria response) {
        this.response = response;
        return this;
    }

}
