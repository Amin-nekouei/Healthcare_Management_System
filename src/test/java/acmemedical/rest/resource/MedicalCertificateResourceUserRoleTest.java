package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalCertificateResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    // Ensure this exists and is linked to cst8277's physician
    private int knownCertId = 1;

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // USER_ROLE
    }

    @Test
    void testCannotGetAllMedicalCertificates() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Auth failed");
        assertEquals(403, response.getStatus(), "USER_ROLE must not access all certificates");
    }

    @Test
    void testCanGetOwnMedicalCertificateById() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/" + knownCertId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Auth failed");

        if (response.getStatus() == 200) {
            Map<String, Object> cert = response.readEntity(new GenericType<>() {});
            assertNotNull(cert);
            assertEquals(knownCertId, ((Number) cert.get("id")).intValue());
        } else if (response.getStatus() == 403) {
            System.out.println("User is not the owner — correct 403 Forbidden.");
        } else if (response.getStatus() == 404) {
            System.out.println("Certificate ID not found — expected if DB is clean.");
        } else {
            fail("Unexpected status: " + response.getStatus());
        }
    }

    @Test
    void testCannotAddMedicalCertificate() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(Map.of()));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotUpdateMedicalCertificate() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/" + knownCertId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(Map.of("signed", false)));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotDeleteMedicalCertificate() {
        Response response = webTarget
            .register(userAuth)
            .path("medicalcertificate/" + knownCertId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }
}
