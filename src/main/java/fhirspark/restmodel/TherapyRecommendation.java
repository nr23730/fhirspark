
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "author",
    "comment",
    "evidenceLevel",
    "id",
    "reasoning",
    "references",
    "treatments"
})
public class TherapyRecommendation {

    @JsonProperty("author")
    private String author;
    @JsonProperty("comment")
    private List<String> comment;
    @JsonProperty("evidenceLevel")
    private String evidenceLevel;
    @JsonProperty("id")
    private String id;
    @JsonProperty("reasoning")
    private Reasoning reasoning;
    @JsonProperty("references")
    private List<Reference> references;
    @JsonProperty("treatments")
    private List<Treatment> treatments;

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    public TherapyRecommendation withAuthor(String author) {
        this.author = author;
        return this;
    }

    @JsonProperty("comment")
    public List<String> getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    public TherapyRecommendation withComment(List<String> comment) {
        this.comment = comment;
        return this;
    }

    @JsonProperty("evidenceLevel")
    public String getEvidenceLevel() {
        return evidenceLevel;
    }

    @JsonProperty("evidenceLevel")
    public void setEvidenceLevel(String evidenceLevel) {
        this.evidenceLevel = evidenceLevel;
    }

    public TherapyRecommendation withEvidenceLevel(String evidenceLevel) {
        this.evidenceLevel = evidenceLevel;
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

    public TherapyRecommendation withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("reasoning")
    public Reasoning getReasoning() {
        return reasoning;
    }

    @JsonProperty("reasoning")
    public void setReasoning(Reasoning reasoning) {
        this.reasoning = reasoning;
    }

    public TherapyRecommendation withReasoning(Reasoning reasoning) {
        this.reasoning = reasoning;
        return this;
    }

    @JsonProperty("references")
    public List<Reference> getReferences() {
        return references;
    }

    @JsonProperty("references")
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public TherapyRecommendation withReferences(List<Reference> references) {
        this.references = references;
        return this;
    }

    @JsonProperty("treatments")
    public List<Treatment> getTreatments() {
        return treatments;
    }

    @JsonProperty("treatments")
    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }

    public TherapyRecommendation withTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
        return this;
    }

}
