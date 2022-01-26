
package fhirspark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 * POJO representing the settings.yaml file.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "port",
    "fhirDbBase",
    "specimenSystem",
    "diagnosticReportSystem",
    "observationSystem",
    "patientSystem",
    "serviceRequestSystem",
    "hgncPath",
    "portalUrl",
    "loginRequired",
    "oncokbPath",
    "hl7v2config",
    "regex"
})
public final class Settings {

    @JsonProperty("port")
    private Integer port;
    @JsonProperty("fhirDbBase")
    private String fhirDbBase;
    @JsonProperty("specimenSystem")
    private String specimenSystem;
    @JsonProperty("diagnosticReportSystem")
    private String diagnosticReportSystem;
    @JsonProperty("observationSystem")
    private String observationSystem;
    @JsonProperty("patientSystem")
    private String patientSystem;
    @JsonProperty("serviceRequestSystem")
    private String serviceRequestSystem;
    @JsonProperty("hgncPath")
    private String hgncPath;
    @JsonProperty("portalUrl")
    private String portalUrl;
    @JsonProperty("loginRequired")
    private Boolean loginRequired;
    @JsonProperty("oncokbPath")
    private String oncokbPath;
    @JsonProperty("hl7v2config")
    private List<Hl7v2config> hl7v2config;
    @JsonProperty("regex")
    private List<Regex> regex;

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty("fhirDbBase")
    public String getFhirDbBase() {
        return fhirDbBase;
    }

    @JsonProperty("fhirDbBase")
    public void setFhirDbBase(String fhirDbBase) {
        this.fhirDbBase = fhirDbBase;
    }

    @JsonProperty("specimenSystem")
    public String getSpecimenSystem() {
        return specimenSystem;
    }

    @JsonProperty("specimenSystem")
    public void setSpecimenSystem(String specimenSystem) {
        this.specimenSystem = specimenSystem;
    }

    @JsonProperty("diagnosticReportSystem")
    public String getDiagnosticReportSystem() {
        return diagnosticReportSystem;
    }

    @JsonProperty("diagnosticReportSystem")
    public void setDiagnosticReportSystem(String diagnosticReportSystem) {
        this.diagnosticReportSystem = diagnosticReportSystem;
    }

    @JsonProperty("observationSystem")
    public String getObservationSystem() {
        return observationSystem;
    }

    @JsonProperty("observationSystem")
    public void setObservationSystem(String observationSystem) {
        this.observationSystem = observationSystem;
    }

    @JsonProperty("patientSystem")
    public String getPatientSystem() {
        return patientSystem;
    }

    @JsonProperty("patientSystem")
    public void setPatientSystem(String patientSystem) {
        this.patientSystem = patientSystem;
    }

    @JsonProperty("serviceRequestSystem")
    public String getServiceRequestSystem() {
        return serviceRequestSystem;
    }

    @JsonProperty("serviceRequestSystem")
    public void setServiceRequestSystem(String serviceRequestSystem) {
        this.serviceRequestSystem = serviceRequestSystem;
    }

    @JsonProperty("hgncPath")
    public String getHgncPath() {
        return hgncPath;
    }

    @JsonProperty("hgncPath")
    public void setHgncPath(String hgncPath) {
        this.hgncPath = hgncPath;
    }

    @JsonProperty("portalUrl")
    public String getPortalUrl() {
        return portalUrl;
    }

    @JsonProperty("portalUrl")
    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    @JsonProperty("loginRequired")
    public Boolean getLoginRequired() {
        return loginRequired;
    }

    @JsonProperty("loginRequired")
    public void setLoginRequired(Boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    @JsonProperty("oncokbPath")
    public String getOncokbPath() {
        return oncokbPath;
    }

    @JsonProperty("oncokbPath")
    public void setOncokbPath(String oncokbPath) {
        this.oncokbPath = oncokbPath;
    }

    @JsonProperty("hl7v2config")
    public List<Hl7v2config> getHl7v2config() {
        return hl7v2config;
    }

    @JsonProperty("hl7v2config")
    public void setHl7v2config(List<Hl7v2config> hl7v2config) {
        this.hl7v2config = hl7v2config;
    }

    @JsonProperty("regex")
    public List<Regex> getRegex() {
        return regex;
    }

    @JsonProperty("regex")
    public void setRegex(List<Regex> newRegex) {
        regex = newRegex;
    }

}
