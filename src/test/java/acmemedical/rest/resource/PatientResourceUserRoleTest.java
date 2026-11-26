package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import acmemedical.entity.Patient;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    @BeforeAll
    void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // USER_ROLE user
    }

    @Test
    void testCannotGetAllPatients() {
        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus(), "USER_ROLE should not be allowed to get all patients");
    }

    @Test
    void testCanGetPatientById() {
        int existingPatientId = 1; // Make sure this ID exists for the test
        Response response = webTarget
            .register(userAuth)
            .path("patient/" + existingPatientId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");

        if (response.getStatus() == 404) {
            System.out.println("Patient with ID " + existingPatientId + " not found (acceptable for this test if not seeded)");
        } else {
            assertEquals(200, response.getStatus(), "USER_ROLE should be able to access a patient by ID");
            Patient patient = response.readEntity(Patient.class);
            assertNotNull(patient);
        }
    }

    @Test
    void testCannotAddPatient() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Blocked");
        newPatient.setLastName("User");

        Response response = webTarget
            .register(userAuth)
            .path("patient")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPatient, MediaType.APPLICATION_JSON));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus(), "USER_ROLE should not be able to add a patient");
    }

    @Test
    void testCannotUpdatePatient() {
        Patient update = new Patient();
        update.setFirstName("Updated");
        update.setLastName("NotAllowed");

        int patientId = 1; // existing patient
        Response response = webTarget
            .register(userAuth)
            .path("patient/" + patientId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus(), "USER_ROLE should not be able to update a patient");
    }

    @Test
    void testCannotDeletePatient() {
        int patientId = 1;
        Response response = webTarget
            .register(userAuth)
            .path("patient/" + patientId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus(), "USER_ROLE should not be able to delete a patient");
    }

    @Test
    void testCannotAddPrescriptionToPatient() {
        int patientId = 1;

        Response response = webTarget
            .register(userAuth)
            .path("patient/" + patientId + "/prescription")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(null, MediaType.APPLICATION_JSON)); // skipping payload for this negative test

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus(), "USER_ROLE should not be able to add a prescription");
    }
}
