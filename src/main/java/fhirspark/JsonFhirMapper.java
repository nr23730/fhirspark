package fhirspark;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

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

        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

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