package fhirspark;

import java.util.HashMap;
import java.util.List;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.JsonContext;
import com.jsoniter.output.JsonStream;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public class JsonFhirMapper {

    FhirContext ctx = FhirContext.forR4();
    IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/hapi-fhir-jpaserver/fhir/");

    public void fromJson(String jsonString) {

        DocumentContext jsonContext = JsonPath.parse(jsonString);
        String patientId = jsonContext.read("$.id");

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        Patient patient = getOrCreatePatient(bundle, patientId);

        List<HashMap<String,Object>> therapyRecommendations = jsonContext.read("$.therapyRecommendations.*");

        for(HashMap<String,Object> therapyRecommendation : therapyRecommendations){
            CarePlan carePlan = new CarePlan();
            carePlan.setSubject(new Reference(patient));

            carePlan.addIdentifier(new Identifier().setSystem("cbioportal").setValue((String)therapyRecommendation.get("id")));
            carePlan.addNote(new Annotation().setText((String)therapyRecommendation.get("comment")));

            bundle.addEntry().setFullUrl(carePlan.getIdElement().getValue()).setResource(carePlan).getRequest()
                    .setUrl("CarePlan").setMethod(Bundle.HTTPVerb.POST);
        }

        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

    }

    public String toJson(String patientId) {
        HashMap<String,Object> jsonMap = new HashMap<String,Object>();

        jsonMap.put("id", "patientId");

        Bundle b2 = (Bundle) client.search().forResource(CarePlan.class)
        .where(new TokenClientParam("identifier").exactly().systemAndCode("cbioportal", patientId))
        .prettyPrint().execute();

        Patient p = (Patient) b2.getEntryFirstRep().getResource();

        return JsonStream.serialize(jsonMap);
    }

    private Patient getOrCreatePatient(Bundle b, String patientId) {

        Bundle b2 = (Bundle) client.search().forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode("cbioportal", patientId))
                .prettyPrint().execute();

        Patient p = (Patient) b2.getEntryFirstRep().getResource();

        if (p != null && p.getIdentifierFirstRep().hasValue()) {
            return p;
        } else {

            Patient patient = new Patient();
            patient.addIdentifier(new Identifier().setSystem("cbioportal").setValue(patientId));
            b.addEntry().setFullUrl(patient.getIdElement().getValue()).setResource(patient).getRequest()
                    .setUrl("Patient").setMethod(Bundle.HTTPVerb.POST);
            return patient;
        }

    }

}