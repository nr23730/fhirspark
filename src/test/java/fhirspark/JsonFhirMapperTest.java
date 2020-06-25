package fhirspark;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import fhirspark.restmodel.*;
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
            settings.setFhirDbBase("https://cbioportal.mi.nr205.de/fhir/");
            this.jfm = new JsonFhirMapper(settings);
        } catch (FileNotFoundException e) {
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