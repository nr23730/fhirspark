package fhirspark;

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
import java.util.Properties;

import fhirspark.resolver.HgncGeneName;
import fhirspark.resolver.OncoKbDrug;
import fhirspark.restmodel.*;
import spark.resource.ClassPathResource;

@TestInstance(Lifecycle.PER_CLASS)
public class JsonFhirMapperTest {

    byte[] inputBytes;
    CbioportalRest inputObject;
    ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
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
            this.jfm = new JsonFhirMapper(settings);
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
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
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
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneMtbTwoRecommendationTest() {
        System.out.println("oneMtbTwoRecommendationTest.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneMtbTwoRecommendationTest.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void twoMtbTwoRecommendationTest() {
        System.out.println("twoMtbTwoRecommendationTest.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("twoMtbTwoRecommendationTest.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void twoMtbThreeRecommendationTest() {
        System.out.println("twoMtbThreeRecommendationTest.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("twoMtbThreeRecommendationTest.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void oneAlterationTwoRecommendationTest() {
        System.out.println("oneAlterationTwoRecommendationTest.json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream("oneAlterationTwoRecommendationTest.json")
                    .readAllBytes();
            inputObject = objectMapper.readValue(inputBytes, CbioportalRest.class);
            jfm.addOrEditMtb(inputObject.getId(), inputObject.getMtbs());
            assertEquals(objectMapper.readTree(inputBytes), objectMapper.readTree(jfm.toJson(inputObject.getId())));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void unknownDrugTest() {
    }

    @Test
    public void samplemanagerTest() {
    }

}