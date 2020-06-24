package fhirspark.resolver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ncitCode", "drugName", "uuid", "synonyms" })
public class Drug {

    @JsonProperty("ncitCode")
    private String ncitCode;
    @JsonProperty("drugName")
    private String drugName;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("synonyms")
    private List<String> synonyms;

    @JsonProperty("ncitCode")
    public String getNcitCode() {
        return ncitCode;
    }

    @JsonProperty("ncitCode")
    public void setNcitCode(String ncitCode) {
        this.ncitCode = ncitCode;
    }

    @JsonProperty("drugName")
    public String getDrugName() {
        return drugName;
    }

    @JsonProperty("drugName")
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty("synonyms")
    public List<String> getSynonyms() {
        return synonyms;
    }

    @JsonProperty("synonyms")
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

}
