
package fhirspark.restmodel;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "comment",
    "reasoning",
    "evidenceLevel",
    "modifications",
    "references",
    "treatments"
})
public class TherapyRecommendation_ implements Serializable
{

    @JsonProperty("id")
    private String id;
    @JsonProperty("comment")
    private List<String> comment = null;
    @JsonProperty("reasoning")
    private Reasoning reasoning;
    @JsonProperty("evidenceLevel")
    private String evidenceLevel;
    @JsonProperty("modifications")
    private List<Modification> modifications = null;
    @JsonProperty("references")
    private List<Reference> references = null;
    @JsonProperty("treatments")
    private List<Treatment> treatments = null;
    private final static long serialVersionUID = -4197915226731596267L;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("comment")
    public List<String> getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    @JsonProperty("reasoning")
    public Reasoning getReasoning() {
        return reasoning;
    }

    @JsonProperty("reasoning")
    public void setReasoning(Reasoning reasoning) {
        this.reasoning = reasoning;
    }

    @JsonProperty("evidenceLevel")
    public String getEvidenceLevel() {
        return evidenceLevel;
    }

    @JsonProperty("evidenceLevel")
    public void setEvidenceLevel(String evidenceLevel) {
        this.evidenceLevel = evidenceLevel;
    }

    @JsonProperty("modifications")
    public List<Modification> getModifications() {
        return modifications;
    }

    @JsonProperty("modifications")
    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications;
    }

    @JsonProperty("references")
    public List<Reference> getReferences() {
        return references;
    }

    @JsonProperty("references")
    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    @JsonProperty("treatments")
    public List<Treatment> getTreatments() {
        return treatments;
    }

    @JsonProperty("treatments")
    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("comment", comment).append("reasoning", reasoning).append("evidenceLevel", evidenceLevel).append("modifications", modifications).append("references", references).append("treatments", treatments).toString();
    }

}
