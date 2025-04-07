package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import acmemedical.entity.Medicine;
import acmemedical.entity.Patient;
import acmemedical.entity.Physician;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientResourceTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";
    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;
private Patient patient;
private Physician physician;
    private int createdPatientId;
    private int createdPhysicianId;

    @BeforeAll
    void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @BeforeEach
void setUp() {
    // Create Patient
    patient = new Patient();
    patient.setFirstName("Test");
    patient.setLastName("Patient");
    patient.setYear(1990);
    patient.setAddress("123 Test Street");
    patient.setHeight(175);
    patient.setWeight(70);
    patient.setSmoker((byte) 0);

    Response patientResponse = webTarget
        .register(adminAuth)
        .path("patient")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(patient, MediaType.APPLICATION_JSON));

    assertEquals(200, patientResponse.getStatus());
    createdPatientId = patientResponse.readEntity(Patient.class).getId();

    // Generate unique Physician name
    String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
    physician = new Physician();
    physician.setFirstName("Unit_" + uniqueSuffix);
    physician.setLastName("Test");

    Response physicianResponse = webTarget
        .register(adminAuth)
        .path("physician")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(physician, MediaType.APPLICATION_JSON));

    assertEquals(200, physicianResponse.getStatus());
    createdPhysicianId = physicianResponse.readEntity(Physician.class).getId();
}


@AfterEach
void tearDown() {
    // Defensive cleanup to avoid leftover test data
    if (createdPatientId != 0) {
        Response deletePatientResponse = webTarget
            .register(adminAuth)
            .path("patient/" + createdPatientId)
            .request()
            .delete();
        System.out.println("Deleted patient " + createdPatientId + " → status: " + deletePatientResponse.getStatus());
    }

    if (createdPhysicianId != 0) {
        Response deletePhysicianResponse = webTarget
            .register(adminAuth)
            .path("physician/" + createdPhysicianId)
            .request()
            .delete();
        System.out.println("Deleted physician " + createdPhysicianId + " → status: " + deletePhysicianResponse.getStatus());
    }

    createdPatientId = 0;
    createdPhysicianId = 0;
}


    @Test
    void testGetAllPatients() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        List<Patient> patients = response.readEntity(new GenericType<List<Patient>>() {});
        assertNotNull(patients);
        assertThat(patients, is(not(empty())));
    }

    @Test
    void testGetPatientById() {
        Response response = webTarget
            .register(adminAuth)
            .path("patient/" + createdPatientId)
            .request()
            .get();

        assertEquals(200, response.getStatus());
        Patient fetched = response.readEntity(Patient.class);
        assertEquals("Test", fetched.getFirstName());
    }

    @Test
    void testUpdatePatient() {
        Patient update = new Patient();
        update.setFirstName("Updated");
        update.setLastName("Patient");
        update.setYear(1985);
        update.setAddress("456 Updated Blvd");
        update.setHeight(180);
        update.setWeight(80);
        update.setSmoker((byte) 1);

        Response response = webTarget
            .register(adminAuth)
            .path("patient/" + createdPatientId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(update, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
        Patient updated = response.readEntity(Patient.class);
        assertEquals("Updated", updated.getFirstName());
    }
    @Test
void testDeletePatientById() {
    // Delete the patient created in @BeforeEach
    Response deleteResponse = webTarget
        .register(adminAuth)
        .path("patient/" + createdPatientId)
        .request()
        .delete();

    assertEquals(200, deleteResponse.getStatus());

    // Verify the patient is no longer accessible
    Response getResponse = webTarget
        .register(adminAuth)
        .path("patient/" + createdPatientId)
        .request()
        .get();

    assertEquals(404, getResponse.getStatus());
}

}



