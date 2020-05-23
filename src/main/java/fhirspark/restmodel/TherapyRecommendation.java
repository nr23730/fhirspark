
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
    "comment",
    "evidenceLevel",
    "id",
    "author",
    "reasoning",
    "references",
    "treatments"
})
public class TherapyRecommendation {

    @JsonProperty("comment")
    private List<String> comment = null;
    @JsonProperty("evidenceLevel")
    private String evidenceLevel;
    @JsonProperty("id")
    private String id;
    @JsonProperty("author")
    private String author;
    @JsonProperty("reasoning")
    private Reasoning reasoning;
    @JsonProperty("references")
    private List<Reference> references = null;
    @JsonProperty("treatments")
    private List<Treatment> treatments = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public TherapyRecommendation withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
