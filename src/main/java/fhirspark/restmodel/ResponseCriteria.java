package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pd3",
    "sd3",
    "pr3",
    "cr3",
    "pd6",
    "sd6",
    "pr6",
    "cr6",
    "pd12",
    "sd12",
    "pr12",
    "cr12"
})
public class ResponseCriteria {

    @JsonProperty("pd3")
    private Boolean pd3 = false;
    @JsonProperty("sd3")
    private Boolean sd3 = false;
    @JsonProperty("pr3")
    private Boolean pr3 = false;
    @JsonProperty("cr3")
    private Boolean cr3 = false;
    @JsonProperty("pd3")
    private Boolean pd6 = false;
    @JsonProperty("sd3")
    private Boolean sd6 = false;
    @JsonProperty("pr3")
    private Boolean pr6 = false;
    @JsonProperty("cr3")
    private Boolean cr6 = false;
    @JsonProperty("pd3")
    private Boolean pd12 = false;
    @JsonProperty("sd3")
    private Boolean sd12 = false;
    @JsonProperty("pr3")
    private Boolean pr12 = false;
    @JsonProperty("cr3")
    private Boolean cr12 = false;


    //3 Month Response
    @JsonProperty("pd3")
    public Boolean getPd3() {
        return pd3;
    }
    @JsonProperty("pd3")
    public void setPd3(Boolean pd3) {
        this.pd3 = pd3;
    }
    public ResponseCriteria withPd3(Boolean pd3) {
        this.pd3 = pd3;
        return this;
    }
    
    @JsonProperty("sd3")
    public Boolean getSd3() {
        return sd3;
    }
    @JsonProperty("sd3")
    public void setSd3(Boolean sd3) {
        this.sd3 = sd3;
    }
    public ResponseCriteria withSd3(Boolean sd3) {
        this.sd3 = sd3;
        return this;
    }

    @JsonProperty("pr3")
    public Boolean getPr3() {
        return pr3;
    }
    @JsonProperty("pr3")
    public void setPr3(Boolean pr3) {
        this.pr3 = pr3;
    }
    public ResponseCriteria withPr3(Boolean pr3) {
        this.pr3 = pr3;
        return this;
    }

    @JsonProperty("cr3")
    public Boolean getCr3() {
        return cr3;
    }
    @JsonProperty("cr3")
    public void setCr3(Boolean cr3) {
        this.cr3 = cr3;
    }
    public ResponseCriteria withCr3(Boolean cr3) {
        this.cr3 = cr3;
        return this;
    }

    //6 Month Response
    @JsonProperty("pd6")
    public Boolean getPd6() {
        return pd6;
    }
    @JsonProperty("pd6")
    public void setPd6(Boolean pd6) {
        this.pd6 = pd6;
    }
    public ResponseCriteria withPd6(Boolean pd6) {
        this.pd6 = pd6;
        return this;
    }
    
    @JsonProperty("sd6")
    public Boolean getSd6() {
        return sd6;
    }
    @JsonProperty("sd6")
    public void setSd6(Boolean sd6) {
        this.sd6 = sd6;
    }
    public ResponseCriteria withSd6(Boolean sd6) {
        this.sd6 = sd6;
        return this;
    }

    @JsonProperty("pr6")
    public Boolean getPr6() {
        return pr6;
    }
    @JsonProperty("pr6")
    public void setPr6(Boolean pr6) {
        this.pr6 = pr6;
    }
    public ResponseCriteria withPr6(Boolean pr6) {
        this.pr6 = pr6;
        return this;
    }

    @JsonProperty("cr6")
    public Boolean getCr6() {
        return cr6;
    }
    @JsonProperty("cr6")
    public void setCr6(Boolean cr6) {
        this.cr6 = cr6;
    }
    public ResponseCriteria withCr6(Boolean cr6) {
        this.cr6 = cr6;
        return this;
    }

    //12 Month Response
    @JsonProperty("pd12")
    public Boolean getPd12() {
        return pd12;
    }
    @JsonProperty("pd12")
    public void setPd12(Boolean pd12) {
        this.pd12 = pd12;
    }
    public ResponseCriteria withPd12(Boolean pd12) {
        this.pd12 = pd12;
        return this;
    }
    
    @JsonProperty("sd12")
    public Boolean getSd12() {
        return sd12;
    }
    @JsonProperty("sd12")
    public void setSd12(Boolean sd12) {
        this.sd12 = sd12;
    }
    public ResponseCriteria withSd12(Boolean sd12) {
        this.sd12 = sd12;
        return this;
    }

    @JsonProperty("pr12")
    public Boolean getPr12() {
        return pr12;
    }
    @JsonProperty("pr12")
    public void setPr12(Boolean pr12) {
        this.pr12 = pr12;
    }
    public ResponseCriteria withPr12(Boolean pr12) {
        this.pr12 = pr12;
        return this;
    }

    @JsonProperty("cr12")
    public Boolean getCr12() {
        return cr12;
    }
    @JsonProperty("cr12")
    public void setCr12(Boolean cr12) {
        this.cr12 = cr12;
    }
    public ResponseCriteria withCr12(Boolean cr12) {
        this.cr12 = cr12;
        return this;
    }
}