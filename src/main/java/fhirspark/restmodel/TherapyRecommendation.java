
package fhirspark.restmodel;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "comment",
    "evidenceLevel",
    "id",
    "modifications",
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
    @JsonProperty("modifications")
    private List<Modification> modifications = null;
    @JsonProperty("reasoning")
    private Reasoning reasoning;
    @JsonProperty("references")
    private List<Reference> references = null;
    @JsonProperty("treatments")
    private List<Treatment> treatments = null;

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

    @JsonProperty("modifications")
    public List<Modification> getModifications() {
        return modifications;
    }

    @JsonProperty("modifications")
    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications;
    }

    public TherapyRecommendation withModifications(List<Modification> modifications) {
        this.modifications = modifications;
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
