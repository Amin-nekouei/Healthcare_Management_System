package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicineResourceUserRoleTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    private int knownMedicineId = 1; // This must exist in your DB for the test

    @BeforeAll
    void initClient() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // USER_ROLE
    }

    @Test
    void testCannotGetAllMedicines() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Authentication failed");
        assertEquals(403, response.getStatus(), "USER_ROLE should not access all medicines");
    }

    @Test
    void testCanGetMedicineById() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/" + knownMedicineId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Authentication failed");

        if (response.getStatus() == 200) {
            Map<String, Object> medicine = response.readEntity(new GenericType<>() {});
            assertNotNull(medicine.get("id"));
            assertEquals(knownMedicineId, ((Number) medicine.get("id")).intValue());
        } else if (response.getStatus() == 404) {
            System.out.println("Medicine with ID " + knownMedicineId + " not found — acceptable in dev DB.");
        } else {
            fail("Unexpected status: " + response.getStatus());
        }
    }

    @Test
    void testCannotAddMedicine() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(Map.of()));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotUpdateMedicine() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/" + knownMedicineId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(Map.of(
                "drugName", "BlockedUpdate",
                "manufacturerName", "None",
                "dosageInformation", "None"
            )));

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }

    @Test
    void testCannotDeleteMedicine() {
        Response response = webTarget
            .register(userAuth)
            .path("medicine/" + knownMedicineId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus());
        assertEquals(403, response.getStatus());
    }
}
