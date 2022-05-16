package fhirspark.adapter;

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

    /**
     *
     * @param geneticAlteration Specified genetic mutation / CNV.
     * @return Observation constrained by genomics-reporting IG.
     */
    public Observation fromJson(GeneticAlteration geneticAlteration) {

        Observation variant = new Observation();
        variant.setMeta(new Meta().addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/variant"));

        variant.setStatus(ObservationStatus.FINAL);

        variant.addCategory(new CodeableConcept(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay())));
        variant.setCode(new CodeableConcept(new Coding("http://loinc.org", "69548-6", "Genetic variant assessment")));
        variant.getValueCodeableConcept().addCoding(new Coding("http://loinc.org", "LA9633-4", "Present"));

        if (geneticAlteration.getChromosome() != null && !geneticAlteration.getChromosome().equals("NA")) {
            ObservationComponentComponent chromosome = new ObservationComponentComponent().setCode(new CodeableConcept(
                    new Coding("http://loinc.org", "48001-2", "Cytogenetic (chromosome) location")));
            chromosome.getValueCodeableConcept().addCoding(new Coding().setSystem(ChromosomeHuman.NULL.getSystem())
                    .setCode(geneticAlteration.getChromosome()));
            variant.addComponent(chromosome);
        }

        switch (geneticAlteration.getAlteration()) {
            case "Amplification":
                ObservationComponentComponent amplification = new ObservationComponentComponent()
                        .setCode(new CodeableConcept(
                                new Coding("http://loinc.org", "62378-5", "Chromosome copy number change [Type]")));
                amplification.getValueCodeableConcept()
                        .addCoding(new Coding("http://loinc.org", "LA14033-7", "Copy number gain"));
                variant.addComponent(amplification);
                break;
            case "Deletion":
                ObservationComponentComponent deletion = new ObservationComponentComponent()
                        .setCode(new CodeableConcept(
                                new Coding("http://loinc.org", "62378-5", "Chromosome copy number change [Type]")));
                deletion.getValueCodeableConcept()
                        .addCoding(new Coding("http://loinc.org", "LA14034-5", "Copy number loss"));
                variant.addComponent(deletion);
                break;
            default:
                ObservationComponentComponent hgvsp = new ObservationComponentComponent().setCode(
                        new CodeableConcept(new Coding("http://loinc.org", "48005-3", "Amino acid change (pHGVS)")));
                hgvsp.getValueCodeableConcept().addCoding(new Coding().setSystem("http://varomen.hgvs.org")
                        .setCode("p." + geneticAlteration.getAlteration()));
                variant.addComponent(hgvsp);
                break;
        }

        ObservationComponentComponent variationCode = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "81252-9", "Discrete genetic variant")));
        variationCode.getValueCodeableConcept().addCoding().setSystem("http://www.ncbi.nlm.nih.gov/gene")
                .setCode(String.valueOf(geneticAlteration.getEntrezGeneId()));
        if (geneticAlteration.getClinvar() != null) {
            variationCode.getValueCodeableConcept().addCoding().setSystem("http://www.ncbi.nlm.nih.gov/clinvar")
                    .setCode(String.valueOf(geneticAlteration.getClinvar()));
        }
        if (geneticAlteration.getCosmic() != null) {
            variationCode.getValueCodeableConcept().addCoding()
                    .setSystem("http://cancer.sanger.ac.uk/cancergenome/projects/cosmic")
                    .setCode(String.valueOf(geneticAlteration.getCosmic()));
        }
        variant.addComponent(variationCode);

        Genenames gn = HgncGeneName.resolve(geneticAlteration.getEntrezGeneId());
        assert geneticAlteration.getHugoSymbol().equals(gn.getApprovedSymbol());
        ObservationComponentComponent hgnc = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "48018-6", "Gene studied [ID]")));
        hgnc.getValueCodeableConcept()
                .addCoding(new Coding("http://www.genenames.org/geneId", gn.getHgncId(), gn.getApprovedSymbol()));
        variant.addComponent(hgnc);

        ObservationComponentComponent startEnd = new ObservationComponentComponent().setCode(
                new CodeableConcept(new Coding("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/tbd-codes",
                        "exact-start-end", "Variant exact start and end")));
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
                    .setCode(new CodeableConcept(new Coding("http://loinc.org", "69551-0", "Genomic alt allele [ID]")));
            alt.getValueStringType().setValue(geneticAlteration.getAlt());
            variant.addComponent(alt);
        }

        if (geneticAlteration.getRef() != null) {
            ObservationComponentComponent ref = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(new Coding("http://loinc.org", "69547-8", "Genomic ref allele [ID]")));
            ref.getValueStringType().setValue(geneticAlteration.getRef());
            variant.addComponent(ref);
        }

        if (geneticAlteration.getAlleleFrequency() != null) {
            ObservationComponentComponent af = new ObservationComponentComponent().setCode(new CodeableConcept(
                    new Coding("http://loinc.org", "81258-6", "Sample variant allelic frequency [NFr]")));
            af.getValueQuantity().setSystem("http://unitsofmeasure.org")
                    .setValue(geneticAlteration.getAlleleFrequency());
            variant.addComponent(af);
        }

        if (geneticAlteration.getDbsnp() != null) {
            ObservationComponentComponent dbsnp = new ObservationComponentComponent()
                    .setCode(new CodeableConcept(new Coding("http://loinc.org", "81255-2", "dbSNP [ID]")));
            dbsnp.getValueCodeableConcept().addCoding(
                    new Coding("http://www.ncbi.nlm.nih.gov/projects/SNP", geneticAlteration.getDbsnp(), null));
            variant.addComponent(dbsnp);
        }

        return variant;

    }

}
