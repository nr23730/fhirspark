
package fhirspark.restmodel;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "modified",
    "recommender",
    "timestamp"
})
public class Modification implements Serializable
{

    @JsonProperty("modified")
    private String modified;
    @JsonProperty("recommender")
    private Recommender recommender;
    @JsonProperty("timestamp")
    private String timestamp;
    private final static long serialVersionUID = 7081567173278031069L;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modified", modified).append("recommender", recommender).append("timestamp", timestamp).toString();
    }

}
