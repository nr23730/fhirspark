
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "author",
    "clinicalTrials",
    "comment",
    "evidenceLevel",
    "evidenceLevelExtension",
    "evidenceLevelM3Text",
    "id",
    "studyId",
    "caseId",
    "reasoning",
    "references",
    "treatments"
})
public class TherapyRecommendation {

    @JsonProperty("author")
    private String author;
    @JsonProperty("clinicalTrials")
    private List<ClinicalTrial> clinicalTrial;
    @JsonProperty("comment")
    private List<String> comment;
    @JsonProperty("evidenceLevel")
    private String evidenceLevel;
    @JsonProperty("evidenceLevelExtension")
    private String evidenceLevelExtension;
    @JsonProperty("evidenceLevelM3Text")
    private String evidenceLevelM3Text;
    @JsonProperty("id")
    private String id;
    @JsonProperty("studyId")
    private String studyId;
    @JsonProperty("caseId")
    private String caseId;
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

    @JsonProperty("clinicalTrials")
    public List<ClinicalTrial> getClinicalTrial() {
        return clinicalTrial;
    }
    
    @JsonProperty("clinicalTrials")
    public void setClinicalTrial(List<ClinicalTrial> clinicalTrial) {
        this.clinicalTrial = clinicalTrial;
    }

    public TherapyRecommendation withClinicalTrial(List<ClinicalTrial> clinicalTrial) {
        this.clinicalTrial = clinicalTrial;
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

    @JsonProperty("evidenceLevelExtension")
    public String getEvidenceLevelExtension() {
        return evidenceLevelExtension;
    }

    @JsonProperty("evidenceLevelExtension")
    public void setEvidenceLevelExtension(String evidenceLevelExtension) {
        this.evidenceLevelExtension = evidenceLevelExtension;
    }

    public String getEvidenceLevelM3Text() {
        return evidenceLevelM3Text;
    }

    public void setEvidenceLevelM3Text(String evidenceLevelM3Text) {
        this.evidenceLevelM3Text = evidenceLevelM3Text;
    }

    @JsonProperty("evidenceLevelExtension")
    public TherapyRecommendation withEvidenceLevelExtensio(String evidenceLevelExtension) {
        this.evidenceLevelExtension = evidenceLevelExtension;
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

    @JsonProperty("studyId")
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    @JsonProperty("studyId")
    public String getStudyId(){
        return studyId;
    }
    
    @JsonProperty("studyId")
    public TherapyRecommendation withStudyId(String studyId) {
        this.studyId = studyId;
        return this;
    }

    @JsonProperty("caseId")
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @JsonProperty("caseId")
    public String getCaseId(){
        return caseId;
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
