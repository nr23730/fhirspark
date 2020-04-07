
package fhirspark.restmodel;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "credentials"
})
public class Recommender implements Serializable
{

    @JsonProperty("credentials")
    private String credentials;
    private final static long serialVersionUID = 7439370389905275539L;

    @JsonProperty("credentials")
    public String getCredentials() {
        return credentials;
    }

    @JsonProperty("credentials")
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentials", credentials).toString();
    }

}
