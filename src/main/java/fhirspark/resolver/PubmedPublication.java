package fhirspark.resolver;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class PubmedPublication {

    private Client client = new Client();
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public String resolvePublication(int pubmedId) {
        WebResource webResource = client
                .resource("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=" + pubmedId + "&retmode=json");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("HTTP Error: " + response.getStatus());
        }

        try {
            JsonNode node = objectMapper.readTree(response.getEntityInputStream());
            return node.at("/result/" + pubmedId + "/title").asText();
        } catch (ClientHandlerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UniformInterfaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       return null;
    }

}