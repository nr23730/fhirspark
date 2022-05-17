package fhirspark.adapter;

import fhirspark.definitions.GenomicsReportingEnum;
import fhirspark.definitions.LoincEnum;
import fhirspark.definitions.UriEnum;
import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.model.Genenames;
import fhirspark.restmodel.GeneticAlteration;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.codesystems.ChromosomeHuman;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

/**
 * Maps genetic mutations / CNVs to FHIR Observation.
 */
public class GeneticAlterationsAdapter {

    private GeneticAlterationsAdapter() {
    }

    /**
     *
     * @param geneticAlteration Specified genetic mutation / CNV.
     * @return Observation constrained by genomics-reporting IG.
     */
    public static Observation fromJson(GeneticAlteration geneticAlteration) {

        Observation variant = new Observation();
        variant.setMeta(new Meta().addProfile(GenomicsReportingEnum.VARIANT.system));

        variant.setStatus(ObservationStatus.FINAL);

        variant.addCategory(new CodeableConcept(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay())));
        variant.setCode(new CodeableConcept(LoincEnum.GENETIC_VARIANT_ASSESSMENT.toCoding()));
        variant.getValueCodeableConcept().addCoding(LoincEnum.PRESENT.toCoding());

        if (geneticAlteration.getChromosome() != null && !geneticAlteration.getChromosome().equals("NA")) {
            ObservationComponentComponent chromosome = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(
                            LoincEnum.CYTOGENETIC_CHROMOSOME_LOCATION.toCoding()));
            chromosome.getValueCodeableConcept()
                    .addCoding(new Coding().setSystem(ChromosomeHuman.NULL.getSystem())
                            .setCode(geneticAlteration.getChromosome()));
            variant.addComponent(chromosome);
        }

        switch (geneticAlteration.getAlteration()) {
            case "Amplification":
                ObservationComponentComponent amplification = new ObservationComponentComponent()
                        .setCode(new CodeableConcept(
                                LoincEnum.CHROMOSOME_COPY_NUMBER_CHANGE.toCoding()));
                amplification.getValueCodeableConcept()
                        .addCoding(LoincEnum.COPY_NUMBER_GAIN.toCoding());
                variant.addComponent(amplification);
                break;
            case "Deletion":
                ObservationComponentComponent deletion = new ObservationComponentComponent()
                        .setCode(new CodeableConcept(
                                LoincEnum.CHROMOSOME_COPY_NUMBER_CHANGE.toCoding()));
                deletion.getValueCodeableConcept()
                        .addCoding(LoincEnum.COPY_NUMBER_LOSS.toCoding());
                variant.addComponent(deletion);
                break;
            default:
                ObservationComponentComponent hgvsp = new ObservationComponentComponent().setCode(
                        new CodeableConcept(LoincEnum.AMINO_ACID_CHANGE.toCoding()));
                hgvsp.getValueCodeableConcept().addCoding(new Coding().setSystem(UriEnum.HGVS.uri)
                        .setCode("p." + geneticAlteration.getAlteration()));
                variant.addComponent(hgvsp);
                break;
        }

        ObservationComponentComponent variationCode = new ObservationComponentComponent()
                .setCode(new CodeableConcept(LoincEnum.DISCRETE_GENETIC_VARIANT.toCoding()));
        variationCode.getValueCodeableConcept().addCoding().setSystem(UriEnum.NCBI_GENE.uri)
                .setCode(String.valueOf(geneticAlteration.getEntrezGeneId()));
        if (geneticAlteration.getClinvar() != null) {
            variationCode.getValueCodeableConcept().addCoding().setSystem(UriEnum.CLINVAR.uri)
                    .setCode(String.valueOf(geneticAlteration.getClinvar()));
        }
        if (geneticAlteration.getCosmic() != null) {
            variationCode.getValueCodeableConcept().addCoding()
                    .setSystem(UriEnum.COSMIC.uri)
                    .setCode(String.valueOf(geneticAlteration.getCosmic()));
        }
        variant.addComponent(variationCode);

        Genenames gn = HgncGeneName.resolve(geneticAlteration.getEntrezGeneId());
        assert geneticAlteration.getHugoSymbol().equals(gn.getApprovedSymbol());
        ObservationComponentComponent hgnc = new ObservationComponentComponent()
                .setCode(new CodeableConcept(LoincEnum.GENE_STUDIED.toCoding()));
        hgnc.getValueCodeableConcept()
                .addCoding(new Coding(UriEnum.GENENAMES.uri, gn.getHgncId(), gn.getApprovedSymbol()));
        variant.addComponent(hgnc);

        ObservationComponentComponent startEnd = new ObservationComponentComponent().setCode(
                new CodeableConcept(GenomicsReportingEnum.EXACT_START_END.toCoding()));
        Range startEndRange = startEnd.getValueRange();
        boolean startEndPresent = false;
        if (geneticAlteration.getStart() != null) {
            startEndRange.setLow(new Quantity(geneticAlteration.getStart()));
            startEndPresent = true;
        }
        if (geneticAlteration.getEnd() != null) {
            startEndRange.setHigh(new Quantity(geneticAlteration.getEnd()));
            startEndPresent = true;
        }
        if (startEndPresent) {
            variant.addComponent(startEnd);
        }

        if (geneticAlteration.getAlt() != null) {
            ObservationComponentComponent alt = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(LoincEnum.GENOMIC_ALT_ALLELE.toCoding()));
            alt.getValueStringType().setValue(geneticAlteration.getAlt());
            variant.addComponent(alt);
        }

        if (geneticAlteration.getRef() != null) {
            ObservationComponentComponent ref = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(LoincEnum.GENOMIC_REF_ALLELE.toCoding()));
            ref.getValueStringType().setValue(geneticAlteration.getRef());
            variant.addComponent(ref);
        }

        if (geneticAlteration.getAlleleFrequency() != null) {
            ObservationComponentComponent af = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(
                            LoincEnum.SAMPLE_VARIANT_ALLELE_FREQUENCY.toCoding()));
            af.getValueQuantity().setSystem(UriEnum.UCUM.uri)
                    .setValue(geneticAlteration.getAlleleFrequency());
            variant.addComponent(af);
        }

        if (geneticAlteration.getDbsnp() != null) {
            ObservationComponentComponent dbsnp = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(LoincEnum.DBSNP.toCoding()));
            dbsnp.getValueCodeableConcept().addCoding(
                    new Coding(UriEnum.DBSNP.uri, geneticAlteration.getDbsnp(), null));
            variant.addComponent(dbsnp);
        }

        return variant;

    }

    public static GeneticAlteration toJson(Observation o) {
        GeneticAlteration g = new GeneticAlteration();
        o.getComponent().forEach(variant -> {
            switch (LoincEnum.fromCode(variant.getCode().getCodingFirstRep().getCode())) {
                case AMINO_ACID_CHANGE:
                    g.setAlteration(variant.getValueCodeableConcept().getCodingFirstRep().getCode()
                            .replaceFirst("p.", ""));
                    break;
                case DISCRETE_GENETIC_VARIANT:
                    variant.getValueCodeableConcept().getCoding().forEach(coding -> {
                        switch (UriEnum.fromUri(coding.getSystem())) {
                            case NCBI_GENE:
                                g.setEntrezGeneId(Integer.valueOf(coding.getCode()));
                                break;
                            case CLINVAR:
                                g.setClinvar(Integer.valueOf(coding.getCode()));
                                break;
                            case COSMIC:
                                g.setCosmic(coding.getCode());
                                break;
                            default:
                                break;
                        }
                    });
                    break;
                case GENE_STUDIED:
                    g.setHugoSymbol(
                            variant.getValueCodeableConcept().getCodingFirstRep()
                                    .getDisplay());
                    break;
                case CYTOGENETIC_CHROMOSOME_LOCATION:
                    g.setChromosome(
                            variant.getValueCodeableConcept().getCodingFirstRep()
                                    .getCode());
                    break;
                case SAMPLE_VARIANT_ALLELE_FREQUENCY:
                    g.setAlleleFrequency(variant.getValueQuantity().getValue().doubleValue());
                    break;
                case DBSNP:
                    g.setDbsnp(variant.getValueCodeableConcept().getCodingFirstRep().getCode());
                    break;
                case CHROMOSOME_COPY_NUMBER_CHANGE:
                    switch (LoincEnum.fromCode(variant.getValueCodeableConcept().getCodingFirstRep()
                            .getCode())) {
                        case COPY_NUMBER_GAIN:
                            g.setAlteration("Amplification");
                            break;
                        case COPY_NUMBER_LOSS:
                            g.setAlteration("Deletion");
                            break;
                        default:
                            break;
                    }
                    break;
                case GENOMIC_ALT_ALLELE:
                    g.setAlt(variant.getValueStringType().getValue());
                    break;
                case GENOMIC_REF_ALLELE:
                    g.setRef(variant.getValueStringType().getValue());
                    break;
                case EXACT_START_END:
                    if (variant.getValueRange().getLow().getValue() != null) {
                        g.setStart(Integer
                                .valueOf(variant.getValueRange().getLow().getValue()
                                        .toString()));
                    }
                    if (variant.getValueRange().getHigh().getValue() != null) {
                        g.setEnd(Integer
                                .valueOf(variant.getValueRange().getHigh().getValue()
                                        .toString()));
                    }
                    break;
                default:
                    break;
            }
        });

        return g;
    }

}
