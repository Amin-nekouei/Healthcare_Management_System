package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.DurationAndStatus;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalTrainingResourceAdminRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private Integer createdSchoolId;
    private Integer createdTrainingId;

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @BeforeEach
    void setUp() {
        createdSchoolId = createTestSchool();
        createdTrainingId = createMedicalTrainingForSchool(createdSchoolId);
    }

    @AfterEach
    void tearDown() {
        if (createdTrainingId != null) {
            Response deleteTraining = webTarget.register(adminAuth)
                .path("medicaltraining/" + createdTrainingId)
                .request()
                .delete();
            createdTrainingId = null;
        }

        if (createdSchoolId != null) {
            Response deleteSchool = webTarget.register(adminAuth)
                .path("medicalschool/" + createdSchoolId)
                .request()
                .delete();
            createdSchoolId = null;
        }
    }

    private Integer createTestSchool() {
        Map<String, Object> publicSchoolJson = new HashMap<>();
publicSchoolJson.put("@type", "PublicSchool");
publicSchoolJson.put("name", "Test School " + System.currentTimeMillis());
publicSchoolJson.put("public", true);  // ✅ REQUIRED for Jackson subtype resolution

Response response = webTarget
    .register(adminAuth)
    .path("medicalschool")
    .request(MediaType.APPLICATION_JSON)
    .post(Entity.json(publicSchoolJson));

        assertEquals(200, response.getStatus());
        Map<String, Object> schoolMap = response.readEntity(new GenericType<>() {});
        return ((Number) schoolMap.get("id")).intValue();
    }

    private Integer createMedicalTrainingForSchool(Integer schoolId) {
        MedicalTraining training = new MedicalTraining();
        DurationAndStatus das = new DurationAndStatus();
        das.setActive((byte) 1);
        das.setStartDate(LocalDateTime.of(2025, 5, 1, 0, 0));
        das.setEndDate(LocalDateTime.of(2025, 5, 31, 0, 0));
        training.setDurationAndStatus(das);

        Response response = webTarget.register(adminAuth)
            .path("medicalschool/" + schoolId + "/medicaltraining")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(training, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        Map<String, Object> trainingMap = response.readEntity(new GenericType<>() {});
        return ((Number) trainingMap.get("id")).intValue();
    }

    @Test
    void testGetMedicalTrainingById() {
        Response response = webTarget.register(adminAuth)
            .path("medicaltraining/" + createdTrainingId)
            .request()
            .get();

        assertEquals(200, response.getStatus());
        Map<String, Object> trainingMap = response.readEntity(new GenericType<>() {});
        assertEquals(createdTrainingId, ((Number) trainingMap.get("id")).intValue());
    }

    @Test
    void testGetAllMedicalTrainings() {
        Response response = webTarget.register(adminAuth)
            .path("medicaltraining")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> trainings = response.readEntity(new GenericType<>() {});
        assertNotNull(trainings);
        assertThat(trainings, is(not(empty())));
    }

    @Test
    void testUpdateMedicalTraining() {
        MedicalTraining updatedTraining = new MedicalTraining();
        DurationAndStatus updatedStatus = new DurationAndStatus();
        updatedStatus.setActive((byte) 0);
        updatedStatus.setStartDate(LocalDateTime.of(2025, 6, 1, 0, 0));
        updatedStatus.setEndDate(LocalDateTime.of(2025, 6, 30, 0, 0));
        updatedTraining.setDurationAndStatus(updatedStatus);

        Response response = webTarget.register(adminAuth)
            .path("medicaltraining/" + createdTrainingId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updatedTraining, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
        Map<String, Object> updatedMap = response.readEntity(new GenericType<>() {});
        Map<String, Object> das = (Map<String, Object>) updatedMap.get("durationAndStatus");
        assertEquals(0, ((Number) das.get("active")).intValue());
    }

    @Test
    void testDeleteMedicalTrainingById() {
        Response delete = webTarget.register(adminAuth)
            .path("medicaltraining/" + createdTrainingId)
            .request()
            .delete();

        assertEquals(200, delete.getStatus());

        Response get = webTarget.register(adminAuth)
            .path("medicaltraining/" + createdTrainingId)
            .request()
            .get();

        assertTrue(get.getStatus() == 404 || get.getStatus() == 500); // Edge case after deletion
        createdTrainingId = null; // prevent re-delete in tearDown
    }

    @Test
    void testMedicalTrainingAttachedToCorrectSchool() {
        Response response = webTarget.register(adminAuth)
            .path("medicaltraining/" + createdTrainingId)
            .request()
            .get();

        assertEquals(200, response.getStatus());
        Map<String, Object> trainingMap = response.readEntity(new GenericType<>() {});
        Map<String, Object> schoolMap = (Map<String, Object>) trainingMap.get("medicalSchool");
        assertEquals(createdSchoolId, ((Number) schoolMap.get("id")).intValue());
    }
}
