
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "attributeId",
    "attributeName",
    "value"
})
public class ClinicalData {

    @JsonProperty("attributeId")
    private String attributeId;
    @JsonProperty("attributeName")
    private String attributeName;
    @JsonProperty("value")
    private String value;

    @JsonProperty("attributeId")
    public String getAttributeId() {
        return attributeId;
    }

    @JsonProperty("attributeId")
    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public ClinicalData withAttributeId(String attributeId) {
        this.attributeId = attributeId;
        return this;
    }

    @JsonProperty("attributeName")
    public String getAttributeName() {
        return attributeName;
    }

    @JsonProperty("attributeName")
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public ClinicalData withAttributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    public ClinicalData withValue(String value) {
        this.value = value;
        return this;
    }

}
