package fhirspark;

import static spark.Spark.*;

import java.io.FileInputStream;
import java.io.InputStream;

public final class FhirSpark {

    private static JsonFhirMapper jsonFhirMapper;

    public static void main(final String[] args) throws Exception {
        InputStream settingsYaml = ClassLoader.getSystemClassLoader().getResourceAsStream("settings.yaml");
        if(args.length == 1)
            settingsYaml = new FileInputStream(args[0]);
        //final Settings settings = objectMapper.readValue(settingsYaml, Settings.class);
        ConfigurationLoader configLoader = new ConfigurationLoader();
        final Settings settings =
                configLoader.loadConfiguration(
                    settingsYaml, Settings.class);
        jsonFhirMapper = new JsonFhirMapper(settings);

        port(settings.getPort());

        options("/mtb/:patientId", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "GET, PUT");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        get("/mtb/:patientId", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.body(jsonFhirMapper.toJson(req.params(":patientId")));
            return res.body();
        });

        options("/mtb/:patientId/mtb/:therapyRecommendation", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "DELETE,PUT");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        delete("/mtb/:patientId/mtb/:therapyRecommendation", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.deleteTherapyRecommendation(req.params(":patientId"), req.params(":therapyRecommendation"));
            res.body(req.body());
            return res.body();
        });

        put("/mtb/:patientId/mtb/:therapyRecommendation", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.editTherapyRecommendation(req.params(":patientId"), req.params(":therapyRecommendation"), req.body());
            res.body(req.body());
            return res.body();
        });

        options("/mtb/:patientId/therapyRecommendation", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "POST");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        put("/mtb/:patientId", (req, res) -> {
            res.status(201);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.addTherapyRecommendation(req.params(":patientId"), req.body());
            res.body(req.body());
            return res.body();
        });

        options("/mtb/:patientId/geneticCounselingRecommended", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "PUT");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        put("/mtb/:patientId/geneticCounselingRecommended", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.editGeneticCounselingRecommendation(req.params(":patient"), req.body());
            return res.body();
        });

        options("/mtb/:patientId/rebiopsyRecommended", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "PUT");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        put("/mtb/:patientId/rebiopsyRecommended", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.editRebiopsyRecommendation(req.params(":patient"), req.body());
            res.body(req.body());
            return res.body();
        });

        options("/mtb/:patientId/comment", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "PUT");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        put("/mtb/:patientId/comment", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.editComment(req.params(":patient"), req.body());
            res.body(req.body());
            return res.body();
        });

    }
    
}
