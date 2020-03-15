package fhirspark;

import static spark.Spark.*;

import java.util.HashMap;

/**
 * Hello world!
 */
public final class App {

    private static JsonFhirMapper jsonFhirMapper = new JsonFhirMapper();

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) { 
        port(3001);
        
        options("/patients/:id", (req, res) -> {
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

        get("/patients/:id", (req,res)->{
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            res.body(jsonFhirMapper.toJson(req.params(":id")));
            return res.body();
        });

        put("/patients/:id", (req,res)->{
            res.status(200);
            res.header("Access-Control-Allow-Credentials", "true");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.type("application/json");
            res.header("Vary", "Origin, Access-Control-Request-Headers");
            jsonFhirMapper.fromJson(req.body());
            res.body(req.body());
            return res.body();
        });
    }
}
