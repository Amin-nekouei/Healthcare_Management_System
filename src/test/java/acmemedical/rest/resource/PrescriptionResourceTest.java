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

import acmemedical.entity.Medicine;
import acmemedical.entity.Patient;
import acmemedical.entity.Physician;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.GenericType;

class PrescriptionResourceTest {

    private static WebTarget webTarget;
    private static HttpAuthenticationFeature adminAuth;
    private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

    @BeforeAll
    static void setUpBeforeClass() {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
    }

    private Physician testPhysician;
    private Patient testPatient;
    private Medicine testMedicine;
    private Integer createdPhysicianId;
    private Integer createdPatientId;
    private Integer createdMedicineId;
    
    @BeforeEach
    void setUp() {
        // Create a Physician
        testPhysician = new Physician();
        testPhysician.setFirstName("test");
        testPhysician.setLastName("test");

        Response response = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(testPhysician, MediaType.APPLICATION_JSON));
        Physician created = response.readEntity(Physician.class);
        createdPhysicianId = created.getId();
   
    
        testPatient = new Patient();
        testPatient.setFirstName("Test");
        testPatient.setLastName("Patient");
        testPatient.setYear(1990);
        testPatient.setAddress("123 Test Street");
        testPatient.setHeight(175);
        testPatient.setWeight(70);
        testPatient.setSmoker((byte) 0);

        Response patientResponse = webTarget
            .register(adminAuth)
            .path("patient")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(testPatient, MediaType.APPLICATION_JSON));

        createdPatientId = patientResponse.readEntity(Patient.class).getId();
    
        // Create a Medicine
        Medicine medicine = new Medicine();
        medicine.setDrugName("Ibuprofen");
        medicine.setManufacturerName("Generic Pharma");
        medicine.setDosageInformation("Take one tablet twice a day");
    
        Response medResponse = webTarget
            .register(adminAuth)
            .path("medicine")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(medicine, MediaType.APPLICATION_JSON));
            testMedicine = medResponse.readEntity(Medicine.class);
            createdMedicineId = testMedicine.getId();

    }
    

    @AfterEach
    void tearDown() {
        if (createdPatientId != null)
            webTarget.register(adminAuth).path("patient/" + createdPatientId).request().delete();
        if (createdPhysicianId != null)
            webTarget.register(adminAuth).path("physician/" + createdPhysicianId).request().delete();
        if (createdMedicineId != null)
            webTarget.register(adminAuth).path("medicine/" + createdMedicineId).request().delete();
    }

    void testAddPrescription() {
        Prescription prescription = new Prescription();
        PrescriptionPK pk = new PrescriptionPK(createdPhysicianId, createdPatientId);
        prescription.setId(pk);

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
    void testGetPrescriptionById() {
        testAddPrescription();

        Response response = webTarget
            .register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request()
            .get();

        assertEquals(200, response.getStatus());

        Prescription result = response.readEntity(Prescription.class);
        assertNotNull(result);
        assertEquals(createdPhysicianId, result.getId().getPhysicianId());
    }

    @Test
    void testUpdatePrescription() {
        testAddPrescription();

        Prescription updated = new Prescription();
        PrescriptionPK pk = new PrescriptionPK(createdPhysicianId, createdPatientId);
        updated.setId(pk);

        Medicine med = new Medicine();
        med.setId(createdMedicineId);
        updated.setMedicine(med);

        updated.setNumberOfRefills(5);
        updated.setPrescriptionInformation("Updated info");

        Response response = webTarget
            .register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(updated, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());

        Prescription result = response.readEntity(Prescription.class);
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

        assertEquals(200, deleteResponse.getStatus());

        Response getResponse = webTarget
            .register(adminAuth)
            .path("prescription/" + createdPatientId + "/" + createdPhysicianId)
            .request()
            .get();

        assertEquals(204, getResponse.getStatus());
    }

    @Test
    void testGetAllPrescriptions() {
        testAddPrescription();

        Response response = webTarget
            .register(adminAuth)
            .path("prescription")
            .request()
            .get();

        assertEquals(200, response.getStatus());

        List<Prescription> prescriptions = response.readEntity(new GenericType<List<Prescription>>() {});
        assertNotNull(prescriptions);
        assertThat(prescriptions, is(not(empty())));
    }
}
