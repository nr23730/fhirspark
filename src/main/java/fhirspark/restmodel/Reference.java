
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pmid",
    "name"
})
public class Reference {

    @JsonProperty("pmid")
    private Integer pmid;
    @JsonProperty("name")
    private String name;

    @JsonProperty("pmid")
    public Integer getPmid() {
        return pmid;
    }

    @JsonProperty("pmid")
    public void setPmid(Integer pmid) {
        this.pmid = pmid;
    }

    public Reference withPmid(Integer pmid) {
        this.pmid = pmid;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Reference withName(String name) {
        this.name = name;
        return this;
    }

}
