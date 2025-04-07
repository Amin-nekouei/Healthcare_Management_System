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
class MedicalCertificateResourceTestAdminRole {

    private Integer createdPhysicianId;
    private Integer createdCertificateId;
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

    @BeforeAll
    static void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> physicianJson = new HashMap<>();
        physicianJson.put("firstName", "Test");
        physicianJson.put("lastName", "Doctor");
        physicianJson.put("email", "doc" + System.currentTimeMillis() + "@test.com");

        Response physicianResponse = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(physicianJson));
        assertEquals(200, physicianResponse.getStatus());

        Map<String, Object> physicianMap = physicianResponse.readEntity(new GenericType<>() {});
        createdPhysicianId = ((Number) physicianMap.get("id")).intValue();

        Map<String, Object> certJson = new HashMap<>();
        certJson.put("signed", true);
        Map<String, Object> ownerMap = new HashMap<>();
        ownerMap.put("id", createdPhysicianId);
        ownerMap.put("version", 0);
        certJson.put("owner", ownerMap);

        Response certResponse = webTarget
            .register(adminAuth)
            .path("medicalcertificate")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(certJson));
        assertEquals(200, certResponse.getStatus());

        Map<String, Object> createdCertMap = certResponse.readEntity(new GenericType<>() {});
        createdCertificateId = createdCertMap.get("id") != null ? ((Number) createdCertMap.get("id")).intValue() : null;
        assertNotNull(createdCertificateId, "Certificate ID should not be null after creation");
    }

    @AfterEach
    void tearDown() {
        if (createdCertificateId != null) {
            webTarget.register(adminAuth)
                .path("medicalcertificate/" + createdCertificateId)
                .request()
                .delete();
            createdCertificateId = null;
        }

        if (createdPhysicianId != null) {
            webTarget.register(adminAuth)
                .path("physician/" + createdPhysicianId)
                .request()
                .delete();
            createdPhysicianId = null;
        }
    }

    @Test
    void testGetAllMedicalCertificates() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        List<Map<String, Object>> certs = response.readEntity(new GenericType<>() {});
        assertThat(certs, is(not(empty())));
    }

    @Test
    void testGetMedicalCertificateById() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicalcertificate/" + createdCertificateId)
            .request()
            .get();

        assertEquals(200, response.getStatus());
        Map<String, Object> cert = response.readEntity(new GenericType<>() {});
        assertEquals(createdCertificateId, ((Number) cert.get("id")).intValue());
    }

    @Test
    void testDeleteMedicalCertificate() {
        Response deleteResponse = webTarget
            .register(adminAuth)
            .path("medicalcertificate/" + createdCertificateId)
            .request()
            .delete();

        assertEquals(200, deleteResponse.getStatus());

        Response getResponse = webTarget
            .register(adminAuth)
            .path("medicalcertificate/" + createdCertificateId)
            .request()
            .get();

        assertTrue(
            getResponse.getStatus() == 404 || getResponse.getStatus() == 500,
            "Expected 404 or 500 after deletion but got: " + getResponse.getStatus()
        );
        createdCertificateId = null;
    }
}
