package fhirspark;

import java.io.IOException;
import java.util.List;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.v281.datatype.CWE;
import ca.uhn.hl7v2.model.v281.datatype.ST;
import ca.uhn.hl7v2.model.v281.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v281.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v281.message.ORU_R01;
import ca.uhn.hl7v2.model.v281.segment.NTE;
import ca.uhn.hl7v2.model.v281.segment.OBR;
import ca.uhn.hl7v2.model.v281.segment.OBX;
import ca.uhn.hl7v2.model.v281.segment.SPM;
import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.PubmedPublication;
import fhirspark.resolver.model.genenames.Doc;
import fhirspark.restmodel.GeneticAlteration;
import fhirspark.restmodel.Mtb;
import fhirspark.restmodel.TherapyRecommendation;
import fhirspark.restmodel.Treatment;

public class JsonHl7v2Mapper {

    Settings settings;
    HapiContext context = new DefaultHapiContext();
    Connection connection;
    PubmedPublication pubmedResolver = new PubmedPublication();

    public JsonHl7v2Mapper(Settings settings) throws HL7Exception {
        this.settings = settings;
        this.connection = context.newClient(settings.getHl7v2config().get(0).getServer(),
                settings.getHl7v2config().get(0).getPort(), false);
    }

    public void toHl7v2Oru(String patientId, List<Mtb> mtbs) throws HL7Exception, IOException, LLPException {
        ORU_R01 oru = new ORU_R01();
        oru.initQuickstart("ORU", "R01", "P");

        for (Mtb mtb : mtbs) {

            // Send only finished MTB results
            if (mtb.getMtbState() == null || !mtb.getMtbState().toUpperCase().equals("COMPLETED"))
                continue;

            for (TherapyRecommendation therapyRecommendation : mtb.getTherapyRecommendations()) {
                ORU_R01_PATIENT_RESULT result = oru.insertPATIENT_RESULT(oru.getPATIENT_RESULTReps());
                result.getPATIENT().getPID().getPid1_SetIDPID().setValue("1");
                result.getPATIENT().getPID()
                        .getPatientIdentifierList(result.getPATIENT().getPID().getPatientIdentifierListReps())
                        .getIDNumber().setValue(patientId);

                int therapyRecommendationOrder = result.getORDER_OBSERVATIONReps();

                OBR masterPanel = result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBR();
                masterPanel.getSetIDOBR().setValue(String.valueOf(result.getORDER_OBSERVATIONReps()));
                masterPanel.getResultStatus().setValue("F");
                masterPanel.getUniversalServiceIdentifier().getIdentifier().setValue("81247-9");
                masterPanel.getUniversalServiceIdentifier().getText()
                        .setValue("Master HL7 genetic variant reporting panel");
                masterPanel.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LN");
                masterPanel.getUniversalServiceIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                masterPanel.getFillerOrderNumber().getEntityIdentifier().setValue(therapyRecommendation.getId());

                masterPanel.getObservationDateTime().setValue(mtb.getDate().replaceAll("-", ""));

                for (String sample : mtb.getSamples()) {
                    SPM specimen = result.getORDER_OBSERVATION(therapyRecommendationOrder)
                            .getSPECIMEN(result.getORDER_OBSERVATION(therapyRecommendationOrder).getSPECIMENReps())
                            .getSPM();
                    specimen.getSetIDSPM().setValue(String.valueOf(
                            result.getORDER_OBSERVATION(result.getORDER_OBSERVATIONReps() - 1).getSPECIMENReps()));
                    specimen.getSpecimenID().getFillerAssignedIdentifier().getEntityIdentifier().setValue(sample);
                    specimen.getSpecimenType().getIdentifier().setValue("TUMOR");
                    specimen.getSpecimenType().getText().setValue("Tumor");
                }

                masterPanel.getFillerOrderNumber().getEntityIdentifier().setValue(mtb.getId());

                OBX evidence = result.getORDER_OBSERVATION(therapyRecommendationOrder)
                        .getOBSERVATION(result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBSERVATIONReps())
                        .getOBX();
                evidence.getSetIDOBX().setValue(
                        String.valueOf(result.getORDER_OBSERVATION(therapyRecommendationOrder).getOBSERVATIONReps()));
                evidence.getValueType().setValue("ST");
                evidence.getObservationIdentifier().getIdentifier().setValue("93044-6");
                evidence.getObservationIdentifier().getText().setValue("Level of evidence");
                evidence.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                evidence.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                ST evidenceValue = new ST(oru);
                evidenceValue.setValue(therapyRecommendation.getEvidenceLevel());
                evidence.insertObservationValue(0).setData(evidenceValue);

                for (GeneticAlteration g : therapyRecommendation.getReasoning().getGeneticAlterations()) {
                    int orderNumber = result.getORDER_OBSERVATIONReps();
                    OBR variant = result.insertORDER_OBSERVATION(orderNumber).getOBR();
                    variant.getSetIDOBR().setValue(String.valueOf(result.getORDER_OBSERVATIONReps()));
                    variant.getUniversalServiceIdentifier().getIdentifier().setValue("81250-3");
                    variant.getUniversalServiceIdentifier().getText().setValue("Discrete genetic variant panel");
                    variant.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LN");
                    variant.getUniversalServiceIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");

                    OBX observation = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(0).getOBX();
                    observation.getSetIDOBX().setValue(String.valueOf(1));
                    observation.getObservationIdentifier().getIdentifier().setValue("69548-6");
                    observation.getObservationIdentifier().getText().setValue("Genetic variant assessment");
                    observation.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation.getValueType().setValue("CWE");
                    CWE c0 = new CWE(oru);
                    c0.getNameOfCodingSystem().setValue("LN");
                    c0.getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    c0.getText().setValue("Present");
                    c0.getIdentifier().setValue("LA9633-4");
                    observation.insertObservationValue(0).setData(c0);

                    OBX observation0 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(1).getOBX();
                    observation0.getSetIDOBX().setValue(String.valueOf(2));
                    observation0.getObservationIdentifier().getIdentifier().setValue("48005-3");
                    observation0.getObservationIdentifier().getText().setValue("Amino acid change (pHGVS)");
                    observation0.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation0.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation0.getValueType().setValue("CWE");
                    CWE c1 = new CWE(oru);
                    c1.getCodingSystemOID().setValue("2.16.840.1.113883.6.282");
                    c1.getText().setValue("p." + g.getAlteration());
                    c1.getIdentifier().setValue("p." + g.getAlteration());
                    observation0.insertObservationValue(0).setData(c1);

                    OBX observation1 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(2).getOBX();
                    observation1.getSetIDOBX().setValue(String.valueOf(3));
                    observation1.getObservationIdentifier().getIdentifier().setValue("81252-9");
                    observation1.getObservationIdentifier().getText().setValue("Discrete genetic variant");
                    observation1.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation1.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation1.getValueType().setValue("CWE");
                    CWE c2 = new CWE(oru);
                    c2.getCodingSystemOID().setValue("2.16.840.1.113883.4.642.3.1041");
                    c2.getText().setValue(String.valueOf(g.getEntrezGeneId()));
                    c2.getIdentifier().setValue(String.valueOf(g.getEntrezGeneId()));
                    observation1.insertObservationValue(0).setData(c2);

                    OBX observation2 = result.getORDER_OBSERVATION(orderNumber).getOBSERVATION(3).getOBX();
                    observation2.getSetIDOBX().setValue(String.valueOf(4));
                    observation2.getObservationIdentifier().getIdentifier().setValue("48018-6");
                    observation2.getObservationIdentifier().getText().setValue("Gene studied [ID]");
                    observation2.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
                    observation2.getObservationIdentifier().getCodingSystemOID().setValue("2.16.840.1.113883.6.1");
                    observation2.getValueType().setValue("CWE");
                    CWE c3 = new CWE(oru);
                    HgncGeneName hgncGeneName = new HgncGeneName();
                    Doc hgncData = hgncGeneName.resolve(g.getEntrezGeneId()).getResponse().getDocs().get(0);
                    c3.getCodingSystemOID().setValue("2.16.840.1.113883.6.281");
                    c3.getIdentifier().setValue(hgncData.getHgncId());
                    c3.getText().setValue(hgncData.getSymbol());
                    observation2.insertObservationValue(0).setData(c3);

                }

                for (fhirspark.restmodel.Reference reference : therapyRecommendation.getReferences()) {
                    CWE v2ref = new CWE(oru);
                    v2ref.getCodingSystemOID().setValue("2.16.840.1.113883.13.191");
                    v2ref.getIdentifier().setValue(String.valueOf(reference.getPmid()));
                    String name = reference.getName() != null ? reference.getName()
                            : pubmedResolver.resolvePublication(reference.getPmid());
                    v2ref.getText().setValue(name);
                }

                for(Treatment treatment : therapyRecommendation.getTreatments()) {
                    CWE v2treatment = new CWE(oru);
                    v2treatment.getCodingSystemOID().setValue("2.16.840.1.113883.3.26.1.1");
                    v2treatment.getIdentifier().setValue(treatment.getNcitCode());
                    v2treatment.getText().setValue(treatment.getName());
                }

                NTE generealRecommendation = result.getORDER_OBSERVATION(therapyRecommendationOrder).getNTE(0);
                generealRecommendation.getSetIDNTE().setValue("1");
                generealRecommendation.getCommentType().getIdentifier().setValue("GI");
                generealRecommendation.getCommentType().getText().setValue("General Instructions");
                generealRecommendation.getComment(0).setValue(mtb.getGeneralRecommendation());

                NTE comments = result.getORDER_OBSERVATION(therapyRecommendationOrder).getNTE(1);
                comments.getSetIDNTE().setValue("2");
                comments.getCommentType().getIdentifier().setValue("1R");
                comments.getCommentType().getText().setValue("Primary Reason");
                for (int i = 0; i < therapyRecommendation.getComment().size(); i++) {
                    comments.getComment(i).setValue(therapyRecommendation.getComment().get(i));
                }

                for (int k = therapyRecommendationOrder; k < result.getORDER_OBSERVATIONReps(); k++)
                    for (ORU_R01_OBSERVATION observation : result.getORDER_OBSERVATION(k).getOBSERVATIONAll())
                        observation.getOBX().insertResponsibleObserver(0).getPersonIdentifier()
                                .setValue(therapyRecommendation.getAuthor());

            }

        }

        connection.getInitiator().sendAndReceive(oru.getMessage());
    }

}