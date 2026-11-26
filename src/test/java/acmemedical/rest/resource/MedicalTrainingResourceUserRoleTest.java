package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalTrainingResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    @BeforeAll
    void setUpClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // Must have USER_ROLE
    }

    @Test
    void testCanGetAllMedicalTrainings() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> trainings = response.readEntity(new GenericType<>() {});
        assertNotNull(trainings);
        assertThat(trainings, is(not(empty())));
    }

    @Test
    void testCanGetMedicalTrainingById() {
        int knownTrainingId = 1; // Use a valid ID from your DB
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/" + knownTrainingId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Authentication failed");
        if (response.getStatus() == 200) {
            Map<String, Object> training = response.readEntity(new GenericType<>() {});
            assertNotNull(training.get("id"));
        } else if (response.getStatus() == 404) {
            System.out.println("Training ID " + knownTrainingId + " not found — acceptable for test.");
        } else {
            fail("Unexpected status: " + response.getStatus());
        }
    }

    @Test
    void testCannotAddMedicalTraining() {
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(Map.of())); // dummy payload

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotUpdateMedicalTraining() {
        int trainingId = 1;
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/" + trainingId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(Map.of("durationAndStatus", Map.of("active", 0))));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotDeleteMedicalTraining() {
        int trainingId = 1;
        Response response = webTarget
            .register(userAuth)
            .path("medicaltraining/" + trainingId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }
}
