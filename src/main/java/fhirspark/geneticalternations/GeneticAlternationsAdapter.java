package fhirspark.geneticalternations;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.model.genenames.Doc;
import fhirspark.restmodel.GeneticAlteration;

public class GeneticAlternationsAdapter {

    private HgncGeneName hgncGeneName = new HgncGeneName();

    public Resource process(GeneticAlteration geneticAlternation) {

        Observation variant = new Observation();
        variant.setMeta(new Meta().addProfile("http://hl7.org/fhir/uv/genomics-reporting/StructureDefinition/variant"));

        variant.setStatus(ObservationStatus.FINAL);

        variant.addCategory(new CodeableConcept(new Coding(ObservationCategory.LABORATORY.getSystem(),
                ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay())));
        variant.setCode(new CodeableConcept(new Coding("http://loinc.org", "69548-6", "Genetic variant assessment")));
        variant.getValueCodeableConcept().addCoding(new Coding("http://loinc.org", "LA9633-4", "Present"));

        ObservationComponentComponent hgvsp = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "48005-3", "Amino acid change (pHGVS)")));
        hgvsp.getValueCodeableConcept().addCoding(
                new Coding().setSystem("http://varomen.hgvs.org").setCode("p." + geneticAlternation.getAlteration()));
        variant.addComponent(hgvsp);

        ObservationComponentComponent ncbiGeneId = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "81252-9", "Discrete genetic variant")));
        ncbiGeneId.getValueCodeableConcept().addCoding(new Coding().setSystem("http://www.ncbi.nlm.nih.gov/clinvar")
                .setCode("" + geneticAlternation.getEntrezGeneId()));
        variant.addComponent(ncbiGeneId);

        Doc hgncData = hgncGeneName.resolve(geneticAlternation.getEntrezGeneId()).getResponse().getDocs().get(0);
        assert (geneticAlternation.getHugoSymbol().equals(hgncData.getSymbol()));
        ObservationComponentComponent hgnc = new ObservationComponentComponent()
                .setCode(new CodeableConcept(new Coding("http://loinc.org", "48018-6", "Gene studied [ID]")));
        hgnc.getValueCodeableConcept()
                .addCoding(new Coding("http://www.genenames.org/geneId", hgncData.getHgncId(), hgncData.getSymbol()));
        variant.addComponent(hgnc);

        return variant;

    }

}