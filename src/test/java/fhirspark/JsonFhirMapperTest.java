package fhirspark;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import fhirspark.restmodel.*;
import spark.resource.ClassPathResource;

import static org.junit.Assert.*;

@TestInstance(Lifecycle.PER_CLASS)
public class JsonFhirMapperTest {

    byte[] inputBytes;
    CbioportalRest inputObject;
    ObjectMapper objectMapper = new ObjectMapper();
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
            this.jfm = new JsonFhirMapper(settings);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void prepare(TestInfo testInfo) {
        System.out.println(testInfo.getTestMethod().get().getName().replace("Test", "") + ".json");
        try {
            inputBytes = ClassLoader
                    .getSystemResourceAsStream(testInfo.getTestMethod().get().getName().replace("Test", "") + ".json")
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
    public void oneMtbZeroRecommendationTest() {
    }

    @Test
    public void oneMtbOneRecommendationTest() {
    }

    @Test
    public void oneMtbTwoRecommendationTest() {
    }

    @Test
    public void twoMtbTwoRecommendationTest() {
    }

    @Test
    public void twoMtbThreeRecommendationTest() {
    }

}