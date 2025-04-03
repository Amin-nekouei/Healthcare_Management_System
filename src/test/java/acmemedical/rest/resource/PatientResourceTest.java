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

        createdPatientId = patientResponse.readEntity(Patient.class).getId();

        physician = new Physician();
        physician.setFirstName("Unit");
        physician.setLastName("Test");

        Response physicianResponse = webTarget
            .register(adminAuth)
            .path("physician")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(physician, MediaType.APPLICATION_JSON));

        createdPhysicianId = physicianResponse.readEntity(Physician.class).getId();
    }

    @AfterEach
    void tearDown() {
        webTarget.register(adminAuth).path("patient/" + createdPatientId).request().delete();
        webTarget.register(adminAuth).path("physician/" + createdPhysicianId).request().delete();
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

    // @Test
    // void testAddPrescriptionToPatient() {
    //     Medicine medicine = new Medicine();
    //     medicine.setId(1);
    //     PrescriptionPK prescriptionPK = new PrescriptionPK();
    //     prescriptionPK.setPatientId(createdPatientId);
    //     prescriptionPK.setPhysicianId(createdPhysicianId);
    

    //     Prescription prescription = new Prescription();
    //     prescription.setId(prescriptionPK);
    //     prescription.setMedicine(medicine);
    //     prescription.setNumberOfRefills(3);
    //     prescription.setPrescriptionInformation("Take once daily");

    //     Response response = webTarget
    //         .register(adminAuth)
    //         .path("patient/" + createdPatientId + "/prescription")
    //         .request(MediaType.APPLICATION_JSON)
    //         .post(Entity.entity(prescription, MediaType.APPLICATION_JSON));

    //     assertEquals(200, response.getStatus());
    //     Patient updated = response.readEntity(Patient.class);
    //     assertNotNull(updated.getPrescriptions());
    //     assertTrue(updated.getPrescriptions().stream().anyMatch(p -> p.getPrescriptionInformation().contains("Take once daily")));
    // }
//     @Test
// void testAddPrescriptionToPatient() {
//     Medicine medicine = new Medicine();
//     medicine.setId(1);
//     PrescriptionPK prescriptionPK = new PrescriptionPK();
//     prescriptionPK.setPatientId(createdPatientId);
//     prescriptionPK.setPhysicianId(createdPhysicianId);
//     Prescription prescription = new Prescription();
//     prescription.setId(prescriptionPK);
//     prescription.setMedicine(medicine);
//     prescription.setNumberOfRefills(3);
//     prescription.setPrescriptionInformation("Take once daily");
//     prescription.setPhysician(physician);
//     prescription.setPatient(patient);
//     Response response = webTarget
//         .register(adminAuth)
//         .path("patient/" + createdPatientId + "/prescription")
//         .request(MediaType.APPLICATION_JSON)
//         .post(Entity.entity(prescription, MediaType.APPLICATION_JSON));


//     assertEquals(200, response.getStatus());
//     Patient updatedPatient = response.readEntity(Patient.class);

//     // Ensure the patient has prescriptions and validate the prescription details
//     assertNotNull(updatedPatient.getPrescriptions());
//     assertThat(updatedPatient.getPrescriptions(), hasSize(greaterThan(0)));
//     assertThat(updatedPatient.getPrescriptions(), hasItem(
//         allOf(
//             hasProperty("prescriptionInformation", containsString("Take once daily")),
//             hasProperty("numberOfRefills", equalTo(3))
//         )
//     ));}

}
