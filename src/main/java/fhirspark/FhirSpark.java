package fhirspark;

import static spark.Spark.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class FhirSpark {

    private static JsonFhirMapper jsonFhirMapper;

    public static void main(final String[] args) throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        InputStream settingsYaml = ClassLoader.getSystemClassLoader().getResourceAsStream("settings.yaml");
        if(args.length == 1)
            settingsYaml = new FileInputStream(args[0]);
        final Settings settings = objectMapper.readValue(settingsYaml, Settings.class);
        jsonFhirMapper = new JsonFhirMapper(settings);

        port(settings.getPort());

        options("/patients/:patientId", (req, res) -> {
            res.status(204);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Headers", req.headers("Access-Control-Request-Headers"));
            res.header("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Content-Length", "0");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.header("Content-Type", "");
            return res;
        });

        get("/patients/:patientId", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.body(jsonFhirMapper.toJson(req.params(":patientId")));
            return res.body();
        });

        put("/patients/:patientId", (req, res) -> {
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.fromJson(req.params("patientId"), req.body());
            res.body(req.body());
            return res.body();
        });
    }
}
