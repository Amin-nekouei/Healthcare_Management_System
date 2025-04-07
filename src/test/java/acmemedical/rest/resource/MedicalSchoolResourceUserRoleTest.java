package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalSchoolResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    private int knownSchoolId = 1; // This should exist for USER tests to run (can be created via admin test setup)

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // USER_ROLE credentials
    }

    @Test
    void testCanGetAllMedicalSchools() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected authenticated access");
        assertEquals(200, response.getStatus());

        List<Map<String, Object>> schools = response.readEntity(new GenericType<>() {});
        assertNotNull(schools);
    }

    @Test
    void testCanGetMedicalSchoolById() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/" + knownSchoolId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected authenticated access");

        if (response.getStatus() == 200) {
            Map<String, Object> school = response.readEntity(new GenericType<>() {});
            assertEquals(knownSchoolId, ((Number) school.get("id")).intValue());
        } else if (response.getStatus() == 404) {
            System.out.println("School with ID " + knownSchoolId + " not found.");
        } else {
            fail("Unexpected response: " + response.getStatus());
        }
    }

    @Test
    void testCannotAddMedicalSchool() {
        Map<String, Object> newSchool = Map.of(
            "@type", "PublicSchool",
            "name", "UserAddAttempt School",
            "public", true
        );

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(newSchool));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotDeleteMedicalSchool() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/" + knownSchoolId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotAddMedicalTrainingToSchool() {
        Map<String, Object> training = Map.of(
            "durationAndStatus", Map.of(
                "active", 1,
                "startDate", "2025-01-01T00:00:00",
                "endDate", "2025-06-01T00:00:00"
            )
        );

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/" + knownSchoolId + "/medicaltraining")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(training));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCanUpdateMedicalSchool() {
        Map<String, Object> update = Map.of(
            "id", knownSchoolId,
            "name", "User Updated Name",
            "public", true
        );

        Response response = webTarget
            .register(userAuth)
            .path("medicalschool/" + knownSchoolId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(update));

        assertNotEquals(401, response.getStatus());

        if (response.getStatus() == 200) {
            Map<String, Object> updated = response.readEntity(new GenericType<>() {});
            assertEquals("User Updated Name", updated.get("name"));
        } else if (response.getStatus() == 404) {
            System.out.println("School not found — update failed.");
        } else if (response.getStatus() == 403) {
            fail("Unexpected 403 on update — check @RolesAllowed");
        }
    }
}
