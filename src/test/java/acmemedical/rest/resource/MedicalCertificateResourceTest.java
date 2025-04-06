package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import acmemedical.entity.MedicalCertificate;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

class MedicalCertificateResourceTest {

    private Integer createdCertificateId;
    private MedicalCertificate testCertificate;
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

    @BeforeAll
    static void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    // @BeforeEach
    // void setUp() {
    //     testCertificate = new MedicalCertificate();
    //     testCertificate.setSigned(true); // use the correct field

    //     Response response = webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate")
    //         .request(MediaType.APPLICATION_JSON)
    //         .post(Entity.entity(testCertificate, MediaType.APPLICATION_JSON));
    //     MedicalCertificate created = response.readEntity(MedicalCertificate.class);
    //     createdCertificateId = created.getId();
    // }

    // @AfterEach
    // void tearDown() {
    //     if (createdCertificateId != null) {
    //         Response response = webTarget
    //             .register(adminAuth)
    //             .path("medicalcertificate/" + createdCertificateId)
    //             .request()
    //             .delete();

    //         System.out.println("Deleted MedicalCertificate ID: " + createdCertificateId + " (status " + response.getStatus() + ")");
    //         createdCertificateId = null;
    //     }
    // }

    @Test
    void testGetAllMedicalCertificates() {
        Response response = webTarget
        .register(adminAuth)
        .path("medicalcertificate")
        .request()
        .get();

    assertEquals(200, response.getStatus(), "Expected HTTP 200 from GET /medicalcertificate");
}

    @Test
    void testGetMedicalCertificateById() {
        Response getResponse = webTarget
            .register(adminAuth)
            .path("medicalcertificate/1")
            .request()
            .get();
    
        assertEquals(200, getResponse.getStatus());
    }
    

    // @Test
    // void testUpdateMedicalCertificate() {
    //     MedicalCertificate cert = webTarget
    //     .register(adminAuth)
    //     .path("medicalcertificate/1")
    //     .request(MediaType.APPLICATION_JSON)
    //     .get()
    //     .readEntity(MedicalCertificate.class);
    //     cert.setSigned(false);

    //     Response putResponse = webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate/1")
    //         .request(MediaType.APPLICATION_JSON)
    //         .put(Entity.entity(cert, MediaType.APPLICATION_JSON));

    //     assertEquals(200, putResponse.getStatus());

    // }

    // @Test
    // void testAddMedicalCertificate() {
    //     MedicalCertificate newCert = new MedicalCertificate();
    //     newCert.setSigned(true);

    //     Response response = webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate")
    //         .request(MediaType.APPLICATION_JSON)
    //         .post(Entity.entity(newCert, MediaType.APPLICATION_JSON));

    //     assertEquals(200, response.getStatus());

    //     MedicalCertificate created = response.readEntity(MedicalCertificate.class);
    //     assertNotNull(created.getId());
    //     assertEquals(1, created.getSigned());

    //     // Clean up
    //     webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate/" + created.getId())
    //         .request()
    //         .delete();
    // }

    // @Test
    // void testDeleteMedicalCertificate() {
    //     Response deleteResponse = webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate/" + createdCertificateId)
    //         .request()
    //         .delete();

    //     assertEquals(200, deleteResponse.getStatus());

    //     Response getResponse = webTarget
    //         .register(adminAuth)
    //         .path("medicalcertificate/" + createdCertificateId)
    //         .request()
    //         .get();

    //     assertEquals(404, getResponse.getStatus());

    //     createdCertificateId = null;
    // }
}
