package fhirspark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import fhirspark.adapter.SpecimenAdapter;
import fhirspark.adapter.TherapyRecommendationAdapter;
import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.*;
import fhirspark.settings.ConfigurationLoader;
import fhirspark.settings.Settings;
import spark.resource.ClassPathResource;

@TestInstance(Lifecycle.PER_CLASS)
public class JsonFhirMapperTest {

    byte[] inputBytes, inputBytesMTB, inputBytesFU;
    CbioportalRest inputObject, inputObjectMTB, inputObjectFU;
    ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    TypeReference<HashMap<String, Object>> type = 
    new TypeReference<HashMap<String, Object>>() {};

    JsonFhirMapper jfm;

    @BeforeAll
    public void prepare() {
        Settings settings;
        try {
            settings = new ConfigurationLoader()
                    .loadConfiguration(new FileInputStream("src/main/resources/settings.yaml"), Settings.class);
            Properties p = new Properties();
            p.load(new ClassPathResource("app.properties").getInputStream());
            settings.setFhirDbBase(p.getProperty("fhir.test.url"));
            HgncGeneName.initialize(settings.getHgncPath());
            OncoKbDrug.initalize(settings.getOncokbPath());
            SpecimenAdapter.initialize(settings.getSpecimenSystem());
            TherapyRecommendationAdapter.initialize(settings.getObservationSystem(), settings.getPatientSystem(), settings.getStudySystem());
            this.jfm = new JsonFhirMapper(settings, false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneMtbZeroRecommendationTest() {
        System.out.println("oneMtbZeroRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneMtbZeroRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneMtbOneRecommendationTest() {
        System.out.println("oneMtbOneRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneMtbOneRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneMtbTwoRecommendationTest() {
        System.out.println("oneMtbTwoRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneMtbTwoRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
            } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void twoMtbTwoRecommendationTest() {
        System.out.println("twoMtbTwoRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("twoMtbTwoRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void twoMtbThreeRecommendationTest() {
        System.out.println("twoMtbThreeRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("twoMtbThreeRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneAlterationTwoRecommendationTest() {
        System.out.println("oneAlterationTwoRecommendation.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneAlterationTwoRecommendation.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneFollowUpOneRecommendationTest() {
        System.out.println("oneFollowUpOneRecommendation.json");
        try {
            inputBytesMTB = ClassLoader
                    .getSystemResourceAsStream("oneMtbOneRecommendation.json")
                    .readAllBytes();
            inputObjectMTB = objectMapper.readValue(inputBytesMTB, CbioportalRest.class);
            jfm.mtbFromJson(inputObjectMTB.getId(), inputObjectMTB.getMtbs());

            inputBytesFU = ClassLoader
                    .getSystemResourceAsStream("oneFollowUpOneRecommendation.json")
                    .readAllBytes();
            inputObjectFU = objectMapper.readValue(inputBytesFU, CbioportalRest.class);
            jfm.followUpFromJson(inputObjectFU.getId(), inputObjectFU.getFollowUps());

            assertEquals(objectMapper.readTree(inputBytesFU), objectMapper.readTree(jfm.followUpToJson(inputObjectFU.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void unknownDrugTest() {
        System.out.println("unknownDrug.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("unknownDrug.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void samplemanagerTest() {
        System.out.println("samplemanager.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("samplemanager.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.mtbFromJson(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.mtbToJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}