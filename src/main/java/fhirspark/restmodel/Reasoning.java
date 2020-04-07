
package fhirspark.restmodel;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "geneticAlterations"
})
public class Reasoning implements Serializable
{

    @JsonProperty("geneticAlterations")
    private List<GeneticAlteration> geneticAlterations = null;
    private final static long serialVersionUID = -8779595048751814431L;

    @JsonProperty("geneticAlterations")
    public List<GeneticAlteration> getGeneticAlterations() {
        return geneticAlterations;
    }

    @JsonProperty("geneticAlterations")
    public void setGeneticAlterations(List<GeneticAlteration> geneticAlterations) {
        this.geneticAlterations = geneticAlterations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("geneticAlterations", geneticAlterations).toString();
    }

}
