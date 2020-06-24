package fhirspark;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import fhirspark.restmodel.CbioportalRest;
import fhirspark.restmodel.Mtb;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.eclipse.jetty.http.HttpStatus;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public final class FhirSpark {

    private static JsonFhirMapper jsonFhirMapper;
    private static JsonHl7v2Mapper jsonHl7v2Mapper;
    private static ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public static void main(final String[] args) throws Exception {
        InputStream settingsYaml = ClassLoader.getSystemClassLoader().getResourceAsStream("settings.yaml");
        if (args.length == 1) {
            settingsYaml = new FileInputStream(args[0]);
        }
        ConfigurationLoader configLoader = new ConfigurationLoader();
        final Settings settings = configLoader.loadConfiguration(settingsYaml, Settings.class);
        jsonFhirMapper = new JsonFhirMapper(settings);
        if (settings.getHl7v2config().get(0).getSendv2()) {
            jsonHl7v2Mapper = new JsonHl7v2Mapper(settings);
        }

        port(settings.getPort());

        options("/mtb/:patientId", (req, res) -> {
            res.status(HttpStatus.NO_CONTENT_204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "GET, PUT, DELETE");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        get("/mtb/:patientId", (req, res) -> {
            res.status(HttpStatus.OK_200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.body(jsonFhirMapper.toJson(req.params(":patientId")));
            return res.body();
        });

        put("/mtb/:patientId", (req, res) -> {
            res.status(HttpStatus.CREATED_201);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");

            List<Mtb> mtbs = objectMapper.readValue(req.body(), CbioportalRest.class).getMtbs();
            jsonFhirMapper.addOrEditMtb(req.params(":patientId"), mtbs);
            if (settings.getHl7v2config().get(0).getSendv2()) {
                jsonHl7v2Mapper.toHl7v2Oru(req.params(":patientId"), mtbs);
            }
            res.body(req.body());
            return res.body();
        });

        delete("/mtb/:patientId", (req, res) -> {
            res.status(HttpStatus.OK_200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.deleteTherapyRecommendation(req.params(":patientId"), req.params(":therapyRecommendation"));
            res.body(req.body());
            return res.body();
        });

    }

}
