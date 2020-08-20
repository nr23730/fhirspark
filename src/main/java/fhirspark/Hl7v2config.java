
package fhirspark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * POJO representing the HL7 v2 configuration in settings.yaml.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sendv2",
    "server",
    "port"
})
public final class Hl7v2config {

    @JsonProperty("sendv2")
    private Boolean sendv2;
    @JsonProperty("server")
    private String server;
    @JsonProperty("port")
    private Integer port;

    @JsonProperty("sendv2")
    public Boolean getSendv2() {
        return sendv2;
    }

    @JsonProperty("sendv2")
    public void setSendv2(Boolean sendv2) {
        this.sendv2 = sendv2;
    }

    @JsonProperty("server")
    public String getServer() {
        return server;
    }

    @JsonProperty("server")
    public void setServer(String server) {
        this.server = server;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

}
