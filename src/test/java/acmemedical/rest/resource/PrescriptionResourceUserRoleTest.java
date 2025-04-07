package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrescriptionResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    // This must exist in your DB and be associated to user if you want auth logic later
    private int testPatientId = 1;
    private int testPhysicianId = 1;

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // USER_ROLE
    }

    @Test
    void testCannotGetAllPrescriptions() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected valid authentication");
        assertEquals(403, response.getStatus(), "USER_ROLE should not access all prescriptions");
    }

    @Test
    void testCanGetOwnPrescriptionById() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription/" + testPatientId + "/" + testPhysicianId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected valid authentication");

        if (response.getStatus() == 200) {
            Map<String, Object> prescription = response.readEntity(new GenericType<>() {});
            assertNotNull(prescription);
            assertEquals(testPatientId, ((Number)((Map<?, ?>)prescription.get("id")).get("patientId")).intValue());
        } else if (response.getStatus() == 404) {
            System.out.println("Prescription not found for patient " + testPatientId + " and physician " + testPhysicianId);
        } else {
            fail("Unexpected response code: " + response.getStatus());
        }
    }

    @Test
    void testCannotCreatePrescription() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json("{}"));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotUpdatePrescription() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription/" + testPatientId + "/" + testPhysicianId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json("{}"));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotDeletePrescription() {
        Response response = webTarget
            .register(userAuth)
            .path("prescription/" + testPatientId + "/" + testPhysicianId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }
}
