package acmemedical.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.*;

import acmemedical.MyObjectMapperProvider;
import acmemedical.entity.*;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

class PrescriptionResourceTest {

    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;

    private Integer createdPhysicianId;
    private Integer createdPatientId;
    private Integer createdMedicineId;

    @BeforeAll
    static void initClient() {
        Client client = ClientBuilder.newClient()
        .register(MyObjectMapperProvider.class);
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    @BeforeEach
    void setUp() {
        // Create Physician
        Physician physician = new Physician();
        physician.setFirstName("Test");
        physician.setLastName("Doctor");

        Response pResponse = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(physician, MediaType.APPLICATION_JSON));
        createdPhysicianId = pResponse.readEntity(Physician.class).getId();

        // Create Patient
        Patient patient = new Patient();
        patient.setFirstName("Test");
        patient.setLastName("Patient");
        patient.setYear(1990);
        patient.setAddress("123 Main St");
        patient.setHeight(170);
        patient.setWeight(70);
        patient.setSmoker((byte) 0);

        Response patResponse = webTarget
            .register(adminAuth)
            .path("patient")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(patient, MediaType.APPLICATION_JSON));
        createdPatientId = patResponse.readEntity(Patient.class).getId();

        // Create Medicine
        Medicine medicine = new Medicine();
        medicine.setDrugName("test");
        medicine.setManufacturerName("test Labs");
        medicine.setDosageInformation("test mg");

        Response mResponse = webTarget
            .register(adminAuth)
            .path("medicine")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(medicine, MediaType.APPLICATION_JSON));
        createdMedicineId = mResponse.readEntity(Medicine.class).getId();
    }

    @AfterEach
void cleanUp() {
    if (createdPatientId != null && createdPhysicianId != null) {
        webTarget.register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request()
            .delete();  // delete prescription first
    }
    if (createdPhysicianId != null)
        webTarget.register(adminAuth).path("physician/" + createdPhysicianId).request().delete();
    if (createdPatientId != null)
        webTarget.register(adminAuth).path("patient/" + createdPatientId).request().delete();
    if (createdMedicineId != null)
        webTarget.register(adminAuth).path("medicine/" + createdMedicineId).request().delete();
}


    void testAddPrescription() {
        Prescription prescription = new Prescription();
        prescription.setId(new PrescriptionPK(createdPhysicianId, createdPatientId));
        prescription.setNumberOfRefills(2);
        prescription.setPrescriptionInformation("Take as needed");

        Medicine med = new Medicine();
        med.setId(createdMedicineId);
        prescription.setMedicine(med);

        Response response = webTarget
            .register(adminAuth)
            .path("prescription")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(prescription, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        Prescription created = response.readEntity(Prescription.class);
        assertNotNull(created);
        assertEquals(createdMedicineId, created.getMedicine().getId());
    }

    @Test
    void testCreatePrescription() {
        testAddPrescription();
    }

    @Test
    void testUpdatePrescription() {
        testAddPrescription();

        Prescription updated = new Prescription();
        updated.setId(new PrescriptionPK(createdPhysicianId, createdPatientId));
        updated.setNumberOfRefills(5);
        updated.setPrescriptionInformation("Updated info");
        Medicine med = new Medicine();
        med.setId(createdMedicineId);
        updated.setMedicine(med);

        Response putResp = webTarget
            .register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updated, MediaType.APPLICATION_JSON));

        assertEquals(200, putResp.getStatus());
        Prescription result = putResp.readEntity(Prescription.class);
        assertEquals(5, result.getNumberOfRefills());
        assertEquals("Updated info", result.getPrescriptionInformation());
    }

    @Test
    void testDeletePrescription() {
        testAddPrescription();

        Response deleteResponse = webTarget
            .register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request()
            .delete();

        assertEquals(204, deleteResponse.getStatus());

 
    }

    @Test
    void testGetAllPrescriptions() {
        testAddPrescription();

        Response resp = webTarget
            .register(adminAuth)
            .path("prescription")
            .request()
            .get();

        assertEquals(200, resp.getStatus());

        List<Prescription> list = resp.readEntity(new GenericType<List<Prescription>>() {});
        assertNotNull(list);
        assertThat(list, is(not(empty())));

    }
    @Test
void testGetPrescriptionById() {
    // First create the prescription
    testAddPrescription();

    // Then retrieve it via the GET endpoint
    Response response = webTarget
        .register(adminAuth)
        .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertEquals(200, response.getStatus(), "Expected 200 OK when fetching prescription by patient and physician ID");

    Prescription fetched = response.readEntity(Prescription.class);
    assertNotNull(fetched, "Fetched prescription should not be null");
    assertEquals(createdPatientId, fetched.getId().getPatientId(), "Patient ID should match");
    assertEquals(createdPhysicianId, fetched.getId().getPhysicianId(), "Physician ID should match");
    assertEquals("Take as needed", fetched.getPrescriptionInformation());
}

} 
