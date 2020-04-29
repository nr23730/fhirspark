package fhirspark.resolver;

import fhirspark.resolver.model.Drug;

import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class OncoKbDrug {

    private Client client = new Client();
    ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public Drug resolveDrug(String name) {
        WebResource webResource = client
                .resource("https://oncokb.org:443/api/v1/drugs/lookup?synonym=" + name + "&exactMatch=true");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("HTTP Error: " + response.getStatus());
        }

        List<Drug> result;
        try {
            result = objectMapper.readValue(response.getEntity(String.class), new TypeReference<List<Drug>>(){});
            return result.get(0);
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