
package fhirspark.restmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "alleleFrequency",
    "alt",
    "alteration",
    "aminoAcidChange",
    "chromosome",
    "clinvar",
    "cosmic",
    "dbsnp",
    "end",
    "entrezGeneId",
    "gnomad",
    "hugoSymbol",
    "ref",
    "start"
})
public class GeneticAlteration {

    @JsonProperty("alleleFrequency")
    private Double alleleFrequency;
    @JsonProperty("alt")
    private String alt;
    @JsonProperty("alteration")
    private String alteration;
    @JsonProperty("aminoAcidChange")
    private String aminoAcidChange;
    @JsonProperty("chromosome")
    private String chromosome;
    @JsonProperty("clinvar")
    private Integer clinvar;
    @JsonProperty("cosmic")
    private String cosmic;
    @JsonProperty("dbsnp")
    private String dbsnp;
    @JsonProperty("end")
    private Integer end;
    @JsonProperty("entrezGeneId")
    private Integer entrezGeneId;
    @JsonProperty("gnomad")
    private Double gnomad;
    @JsonProperty("hugoSymbol")
    private String hugoSymbol;
    @JsonProperty("ref")
    private String ref;
    @JsonProperty("start")
    private Integer start;

    @JsonProperty("alleleFrequency")
    public Double getAlleleFrequency() {
        return alleleFrequency;
    }

    @JsonProperty("alleleFrequency")
    public void setAlleleFrequency(Double alleleFrequency) {
        this.alleleFrequency = alleleFrequency;
    }

    public GeneticAlteration withAlleleFrequency(Double alleleFrequency) {
        this.alleleFrequency = alleleFrequency;
        return this;
    }

    @JsonProperty("alt")
    public String getAlt() {
        return alt;
    }

    @JsonProperty("alt")
    public void setAlt(String alt) {
        this.alt = alt;
    }

    public GeneticAlteration withAlt(String alt) {
        this.alt = alt;
        return this;
    }

    @JsonProperty("alteration")
    public String getAlteration() {
        return alteration;
    }

    @JsonProperty("alteration")
    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public GeneticAlteration withAlteration(String alteration) {
        this.alteration = alteration;
        return this;
    }

    @JsonProperty("aminoAcidChange")
    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    @JsonProperty("aminoAcidChange")
    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public GeneticAlteration withAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
        return this;
    }

    @JsonProperty("chromosome")
    public String getChromosome() {
        return chromosome;
    }

    @JsonProperty("chromosome")
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public GeneticAlteration withChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

    @JsonProperty("clinvar")
    public Integer getClinvar() {
        return clinvar;
    }

    @JsonProperty("clinvar")
    public void setClinvar(Integer clinvar) {
        this.clinvar = clinvar;
    }

    public GeneticAlteration withClinvar(Integer clinvar) {
        this.clinvar = clinvar;
        return this;
    }

    @JsonProperty("cosmic")
    public String getCosmic() {
        return cosmic;
    }

    @JsonProperty("cosmic")
    public void setCosmic(String cosmic) {
        this.cosmic = cosmic;
    }

    public GeneticAlteration withCosmic(String cosmic) {
        this.cosmic = cosmic;
        return this;
    }

    @JsonProperty("dbsnp")
    public String getDbsnp() {
        return dbsnp;
    }

    @JsonProperty("dbsnp")
    public void setDbsnp(String dbsnp) {
        this.dbsnp = dbsnp;
    }

    public GeneticAlteration withDbsnp(String dbsnp) {
        this.dbsnp = dbsnp;
        return this;
    }

    @JsonProperty("end")
    public Integer getEnd() {
        return end;
    }

    @JsonProperty("end")
    public void setEnd(Integer end) {
        this.end = end;
    }

    public GeneticAlteration withEnd(Integer end) {
        this.end = end;
        return this;
    }

    @JsonProperty("entrezGeneId")
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    @JsonProperty("entrezGeneId")
    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public GeneticAlteration withEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
        return this;
    }

    @JsonProperty("gnomad")
    public Double getGnomad() {
        return gnomad;
    }

    @JsonProperty("gnomad")
    public void setGnomad(Double gnomad) {
        this.gnomad = gnomad;
    }

    public GeneticAlteration withGnomad(Double gnomad) {
        this.gnomad = gnomad;
        return this;
    }

    @JsonProperty("hugoSymbol")
    public String getHugoSymbol() {
        return hugoSymbol;
    }

    @JsonProperty("hugoSymbol")
    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public GeneticAlteration withHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
        return this;
    }

    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    @JsonProperty("ref")
    public void setRef(String ref) {
        this.ref = ref;
    }

    public GeneticAlteration withRef(String ref) {
        this.ref = ref;
        return this;
    }

    @JsonProperty("start")
    public Integer getStart() {
        return start;
    }

    @JsonProperty("start")
    public void setStart(Integer start) {
        this.start = start;
    }

    public GeneticAlteration withStart(Integer start) {
        this.start = start;
        return this;
    }

    @JsonAnySetter
    public void otherAttributes(String key, Object value) {
    }

}
