package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import acmemedical.entity.Physician;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

class PhysicianResourceUserRoleTest {

    private static WebTarget webTarget;
    private static HttpAuthenticationFeature userAuth;

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

    @BeforeAll
    static void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277"); // must be a valid USER_ROLE user
    }

    @Test
    void testCannotGetAllPhysicians() {
        Response response = webTarget
            .register(userAuth)
            .path("physician")
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus()); // Forbidden
    }

    @Test
    void testCanGetOwnPhysicianById() {
        int ownPhysicianId = 1;
        Response response = webTarget
            .register(userAuth)
            .path("physician/" + ownPhysicianId)
            .request()
            .get();
    
        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(200, response.getStatus());
    }

    @Test
    void testCannotGetUnrelatedPhysicianById() {
        int unrelatedPhysicianId = 999; // must not be linked to this user
        Response response = webTarget
            .register(userAuth)
            .path("physician/" + unrelatedPhysicianId)
            .request()
            .get();

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus()); // Forbidden
    }

    @Test
    void testCannotAddPhysician() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Blocked");
        newPhysician.setLastName("User");

        Response response = webTarget
            .register(userAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus()); // Forbidden
    }

    @Test
    void testCannotUpdatePhysician() {
        int physicianId = 2;
        Physician update = new Physician();
        update.setFirstName("Updated");
        update.setLastName("Name");

        Response response = webTarget
            .register(userAuth)
            .path("physician/" + physicianId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus()); // Forbidden
    }

    @Test
    void testCannotDeletePhysician() {
        int physicianId = 2;
        Response response = webTarget
            .register(userAuth)
            .path("physician/" + physicianId)
            .request()
            .delete();

        assertNotEquals(401, response.getStatus(), "Expected auth to succeed, but got 401");
        assertEquals(403, response.getStatus()); // Forbidden
    }
}
