
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

    public Modification withModified(String modified) {
        this.modified = modified;
        return this;
    }

    @JsonProperty("recommender")
    public Recommender getRecommender() {
        return recommender;
    }

    @JsonProperty("recommender")
    public void setRecommender(Recommender recommender) {
        this.recommender = recommender;
    }

    public Modification withRecommender(Recommender recommender) {
        this.recommender = recommender;
        return this;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Modification withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}
