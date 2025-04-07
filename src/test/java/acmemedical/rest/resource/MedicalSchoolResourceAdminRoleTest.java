package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalSchoolResourceAdminRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private Integer createdSchoolId;

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @AfterEach
    void cleanup() {
        if (createdSchoolId != null) {
            Response delete = webTarget
                .register(adminAuth)
                .path("medicalschool/" + createdSchoolId)
                .request()
                .delete();

            System.out.println("Deleted school ID " + createdSchoolId + " (status " + delete.getStatus() + ")");
            createdSchoolId = null;
        }
    }

    @Test
    void testGetAllMedicalSchools() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> schools = response.readEntity(new GenericType<>() {});
        assertNotNull(schools);
    }

    @Test
    void testAddMedicalSchool() {
        Map<String, Object> schoolJson = new HashMap<>();
        String name = "AdminTest School " + System.currentTimeMillis();
        schoolJson.put("@type", "PublicSchool");
        schoolJson.put("name", name);
        schoolJson.put("public", true);

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(schoolJson));

        assertEquals(200, response.getStatus());
        Map<String, Object> created = response.readEntity(new GenericType<>() {});
        createdSchoolId = ((Number) created.get("id")).intValue();
        assertTrue(((String) created.get("name")).startsWith("AdminTest School"),
                   "Expected name to start with 'AdminTest School' but got: " + created.get("name"));
    }

    @Test
    void testGetMedicalSchoolById() {
        testAddMedicalSchool(); // ensures createdSchoolId exists

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/" + createdSchoolId)
            .request()
            .get();

        assertEquals(200, response.getStatus());
        Map<String, Object> school = response.readEntity(new GenericType<>() {});
        assertEquals(createdSchoolId, ((Number) school.get("id")).intValue());
    }

    @Test
    void testUpdateMedicalSchool() {
        testAddMedicalSchool(); // ensures createdSchoolId exists

        Map<String, Object> updateJson = new HashMap<>();
        updateJson.put("id", createdSchoolId);
        updateJson.put("name", "Updated Admin School");
        updateJson.put("public", true);

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/" + createdSchoolId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(updateJson));

        assertEquals(200, response.getStatus());
        Map<String, Object> updated = response.readEntity(new GenericType<>() {});
        assertEquals("Updated Admin School", updated.get("name"));
    }

    @Test
    void testAddMedicalTrainingToSchool() {
        testAddMedicalSchool(); // ensures createdSchoolId exists

        Map<String, Object> trainingJson = new HashMap<>();
        trainingJson.put("durationAndStatus", Map.of(
            "active", 1,
            "startDate", "2025-05-01T00:00:00",
            "endDate", "2025-06-01T00:00:00"
        ));

        Response response = webTarget
            .register(adminAuth)
            .path("medicalschool/" + createdSchoolId + "/medicaltraining")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(trainingJson));

        assertEquals(201, response.getStatus());
        Map<String, Object> training = response.readEntity(new GenericType<>() {});
        assertNotNull(training.get("id"));
    }

    @Test
    void testDeleteMedicalSchool() {
        testAddMedicalSchool(); // ensures createdSchoolId exists

        Response delete = webTarget
            .register(adminAuth)
            .path("medicalschool/" + createdSchoolId)
            .request()
            .delete();

        assertEquals(200, delete.getStatus());
        createdSchoolId = null;
    }
}
