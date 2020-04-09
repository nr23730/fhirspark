
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "modified",
    "recommender",
    "timestamp"
})
public class Modification {

    @JsonProperty("modified")
    private String modified;
    @JsonProperty("recommender")
    private Recommender recommender;
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("modified")
    public String getModified() {
        return modified;
    }

    @JsonProperty("modified")
    public void setModified(String modified) {
        this.modified = modified;
    }

    @JsonProperty("recommender")
    public Recommender getRecommender() {
        return recommender;
    }

    @JsonProperty("recommender")
    public void setRecommender(Recommender recommender) {
        this.recommender = recommender;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
