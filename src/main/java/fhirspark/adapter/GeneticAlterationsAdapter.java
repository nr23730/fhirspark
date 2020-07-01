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
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.codesystems.ChromosomeHuman;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

public class GeneticAlterationsAdapter {

    public Resource process(GeneticAlteration geneticAlteration) {

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
                        .addCoding(new Coding("http://loinc.org", "LA14033-7", "Copy number loss"));
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

        ObservationComponentComponent ncbiGeneId = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "81252-9", "Discrete genetic variant")));
        ncbiGeneId.getValueCodeableConcept().addCoding(new Coding().setSystem("http://www.ncbi.nlm.nih.gov/clinvar")
                .setCode("" + geneticAlteration.getEntrezGeneId()));
        variant.addComponent(ncbiGeneId);

        Genenames gn = HgncGeneName.resolve(geneticAlteration.getEntrezGeneId());
        assert geneticAlteration.getHugoSymbol().equals(gn.getApprovedSymbol());
        ObservationComponentComponent hgnc = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "48018-6", "Gene studied [ID]")));
        hgnc.getValueCodeableConcept()
                .addCoding(new Coding("http://www.genenames.org/geneId", gn.getHgncId(), gn.getApprovedSymbol()));
        variant.addComponent(hgnc);

        return variant;

    }

}
