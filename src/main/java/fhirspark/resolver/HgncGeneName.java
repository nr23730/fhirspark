package fhirspark.resolver;

import fhirspark.resolver.model.genenames.Genenames;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class HgncGeneName {

    private Client client = new Client();
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
    
    public Genenames resolve(int ncbiGeneId) {
        WebResource webResource = client
                .resource("http://rest.genenames.org/fetch/entrez_id/" + ncbiGeneId);
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("HTTP Error: " + response.getStatus());
        }

        Genenames result;
        try {
            result = objectMapper.readValue(response.getEntity(String.class), Genenames.class);
            return result;
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientHandlerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UniformInterfaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       return null;
    }

}
