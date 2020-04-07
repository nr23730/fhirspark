
package fhirspark.restmodel;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "ncit_code",
    "synonyms"
})
public class Treatment implements Serializable
{

    @JsonProperty("name")
    private String name;
    @JsonProperty("ncit_code")
    private String ncitCode;
    @JsonProperty("synonyms")
    private String synonyms;
    private final static long serialVersionUID = 5378130599841238154L;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("ncit_code")
    public String getNcitCode() {
        return ncitCode;
    }

    @JsonProperty("ncit_code")
    public void setNcitCode(String ncitCode) {
        this.ncitCode = ncitCode;
    }

    @JsonProperty("synonyms")
    public String getSynonyms() {
        return synonyms;
    }

    @JsonProperty("synonyms")
    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("ncitCode", ncitCode).append("synonyms", synonyms).toString();
    }

}
