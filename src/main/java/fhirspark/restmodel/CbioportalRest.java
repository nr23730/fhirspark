
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "mtbs",
    "followUps"
})
public class CbioportalRest {

    @JsonProperty("id")
    private String id;
    @JsonProperty("mtbs")
    private List<Mtb> mtbs;
    @JsonProperty("mtbs")
    private List<FollowUp> followUps;
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    public CbioportalRest withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("mtbs")
    public List<Mtb> getMtbs() {
        return mtbs;
    }
    @JsonProperty("mtbs")
    public void setMtbs(List<Mtb> mtbs) {
        this.mtbs = mtbs;
    }
    public CbioportalRest withMtbs(List<Mtb> mtbs) {
        this.mtbs = mtbs;
        return this;
    }

    @JsonProperty("followUps")
    public List<FollowUp> getFollowUps() {
        return followUps;
    }
    @JsonProperty("followUps")
    public void setFollowUps(List<FollowUp> followUps) {
        this.followUps = followUps;
    }
    public CbioportalRest withFollowUps(List<FollowUp> followUps) {
        this.followUps = followUps;
        return this;
    }

}
