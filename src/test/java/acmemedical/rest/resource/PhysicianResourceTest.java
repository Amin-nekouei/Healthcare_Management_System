package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import acmemedical.entity.Medicine;
import acmemedical.entity.Physician;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

class PhysicianResourceTest {

    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical/api/v1";

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @BeforeEach
    void setUp() throws Exception {
        // 필요 시 테스트마다 실행되는 초기화
    }

    @Test
    void testGetPhysicians() {
        Response response = webTarget
            .register(adminAuth)
            .path("physician")
            .request()
            .get();

        assertEquals(200, response.getStatus());

        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>() {});
        assertNotNull(physicians);
        assertThat(physicians, is(not(empty())));
    }

    @Test
    void testAddPhysician() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("John");
        newPhysician.setLastName("Doe");
//        newPhysician.setSpecialty("Cardiology");
//        newPhysician.setLicense("JD999");

        Response response = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());

        Physician created = response.readEntity(Physician.class);
        assertNotNull(created.getId());
        assertEquals("John", created.getFirstName());
    }

    @Test
    void testGetPhysicianById() {
        // 먼저 추가
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Grace");
        newPhysician.setLastName("Hopper");
//        newPhysician.setSpecialty("CompSci");
//        newPhysician.setLicense("GH123");

        Response postResponse = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));

        assertEquals(200, postResponse.getStatus());
        Physician created = postResponse.readEntity(Physician.class);
        int newId = created.getId();

        // 다시 조회
        Response getResponse = webTarget
            .register(adminAuth)
            .path("physician/" + newId)
            .request()
            .get();

        assertEquals(200, getResponse.getStatus());
        Physician fetched = getResponse.readEntity(Physician.class);
        assertEquals("Grace", fetched.getFirstName());
    }

    @Test
    void testUpdateMedicineForPhysicianPatient() {
        // 사전에 유효한 physicianId 와 patientId가 DB에 있어야 합니다
        int physicianId = 1;
        int patientId = 2;

        Medicine medicine = new Medicine();
        medicine.setDrugName("Aspirin");

        Response response = webTarget
            .register(adminAuth)
            .path("physician/" + physicianId + "/patient/" + patientId + "/medicine")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(medicine, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
        Medicine updated = response.readEntity(Medicine.class);
        assertEquals("Aspirin", updated.getDrugName());
    }
}
