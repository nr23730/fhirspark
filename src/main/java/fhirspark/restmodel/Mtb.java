
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "author",
    "date",
    "generalRecommendation",
    "geneticCounselingRecommendation",
    "id",
    "mtbState",
    "rebiopsyRecommendation",
    "samples",
    "therapyRecommendations"
})
public class Mtb {

    @JsonProperty("author")
    private String author;
    @JsonProperty("date")
    private String date;
    @JsonProperty("generalRecommendation")
    private String generalRecommendation;
    @JsonProperty("geneticCounselingRecommendation")
    private Boolean geneticCounselingRecommendation;
    @JsonProperty("id")
    private String id;
    @JsonProperty("mtbState")
    private String mtbState;
    @JsonProperty("rebiopsyRecommendation")
    private Boolean rebiopsyRecommendation;
    @JsonProperty("samples")
    private List<String> samples = null;
    @JsonProperty("therapyRecommendations")
    private List<TherapyRecommendation> therapyRecommendations = null;

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    public Mtb withAuthor(String author) {
        this.author = author;
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

    public Mtb withDate(String date) {
        this.date = date;
        return this;
    }

    @JsonProperty("generalRecommendation")
    public String getGeneralRecommendation() {
        return generalRecommendation;
    }

    @JsonProperty("generalRecommendation")
    public void setGeneralRecommendation(String generalRecommendation) {
        this.generalRecommendation = generalRecommendation;
    }

    public Mtb withGeneralRecommendation(String generalRecommendation) {
        this.generalRecommendation = generalRecommendation;
        return this;
    }

    @JsonProperty("geneticCounselingRecommendation")
    public Boolean getGeneticCounselingRecommendation() {
        return geneticCounselingRecommendation;
    }

    @JsonProperty("geneticCounselingRecommendation")
    public void setGeneticCounselingRecommendation(Boolean geneticCounselingRecommendation) {
        this.geneticCounselingRecommendation = geneticCounselingRecommendation;
    }

    public Mtb withGeneticCounselingRecommendation(Boolean geneticCounselingRecommendation) {
        this.geneticCounselingRecommendation = geneticCounselingRecommendation;
        return this;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Mtb withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("mtbState")
    public String getMtbState() {
        return mtbState;
    }

    @JsonProperty("mtbState")
    public void setMtbState(String mtbState) {
        this.mtbState = mtbState;
    }

    public Mtb withMtbState(String mtbState) {
        this.mtbState = mtbState;
        return this;
    }

    @JsonProperty("rebiopsyRecommendation")
    public Boolean getRebiopsyRecommendation() {
        return rebiopsyRecommendation;
    }

    @JsonProperty("rebiopsyRecommendation")
    public void setRebiopsyRecommendation(Boolean rebiopsyRecommendation) {
        this.rebiopsyRecommendation = rebiopsyRecommendation;
    }

    public Mtb withRebiopsyRecommendation(Boolean rebiopsyRecommendation) {
        this.rebiopsyRecommendation = rebiopsyRecommendation;
        return this;
    }

    @JsonProperty("samples")
    public List<String> getSamples() {
        return samples;
    }

    @JsonProperty("samples")
    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public Mtb withSamples(List<String> samples) {
        this.samples = samples;
        return this;
    }

    @JsonProperty("therapyRecommendations")
    public List<TherapyRecommendation> getTherapyRecommendations() {
        return therapyRecommendations;
    }

    @JsonProperty("therapyRecommendations")
    public void setTherapyRecommendations(List<TherapyRecommendation> therapyRecommendations) {
        this.therapyRecommendations = therapyRecommendations;
    }

    public Mtb withTherapyRecommendations(List<TherapyRecommendation> therapyRecommendations) {
        this.therapyRecommendations = therapyRecommendations;
        return this;
    }

}
