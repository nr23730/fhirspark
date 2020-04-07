
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
    "therapyRecommendations"
})
public class TherapyRecommendation implements Serializable
{

    @JsonProperty("id")
    private String id;
    @JsonProperty("therapyRecommendations")
    private List<TherapyRecommendation_> therapyRecommendations = null;
    private final static long serialVersionUID = 8346910927803718627L;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("therapyRecommendations")
    public List<TherapyRecommendation_> getTherapyRecommendations() {
        return therapyRecommendations;
    }

    @JsonProperty("therapyRecommendations")
    public void setTherapyRecommendations(List<TherapyRecommendation_> therapyRecommendations) {
        this.therapyRecommendations = therapyRecommendations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("therapyRecommendations", therapyRecommendations).toString();
    }

}
