
package fhirspark.restmodel;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pmid",
    "name"
})
public class Reference implements Serializable
{

    @JsonProperty("pmid")
    private Integer pmid;
    @JsonProperty("name")
    private String name;
    private final static long serialVersionUID = 6952939523846493081L;

    @JsonProperty("pmid")
    public Integer getPmid() {
        return pmid;
    }

    @JsonProperty("pmid")
    public void setPmid(Integer pmid) {
        this.pmid = pmid;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pmid", pmid).append("name", name).toString();
    }

}
