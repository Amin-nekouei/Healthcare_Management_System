// package acmemedical.rest.resource;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.*;

// import java.util.List;

// import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import acmemedical.entity.Medicine;
// import acmemedical.entity.Physician;

// import jakarta.ws.rs.client.Client;
// import jakarta.ws.rs.client.ClientBuilder;
// import jakarta.ws.rs.client.Entity;
// import jakarta.ws.rs.client.WebTarget;
// import jakarta.ws.rs.core.GenericType;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;

// class PhysicianResourceTest {
//     private Integer createdPhysicianId;
//     private Physician testPhysician;
//     private static WebTarget webTarget;
//     private static HttpAuthenticationFeature adminAuth;

//     private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

//     @BeforeAll
//     static void setUpBeforeClass() throws Exception {
//         Client client = ClientBuilder.newClient();
//         webTarget = client.target(BASE_URI);
//         adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
//     }

//     @BeforeEach
//     void setUp() {
//         testPhysician = new Physician();
//         testPhysician.setFirstName("test");
//         testPhysician.setLastName("test");

//         Response response = webTarget
//             .register(adminAuth)
//             .path("physician")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(testPhysician, MediaType.APPLICATION_JSON));

//         assertEquals(200, response.getStatus());

//         Physician created = response.readEntity(Physician.class);
//         createdPhysicianId = created.getId();
//     }

//     @AfterEach
//     void tearDown() {
//         if (createdPhysicianId != null) {
//             Response response = webTarget
//                 .register(adminAuth)
//                 .path("physician/" + createdPhysicianId)
//                 .request()
//                 .delete();

//             System.out.println("Deleted Physician ID: " + createdPhysicianId + " (status " + response.getStatus() + ")");
//             createdPhysicianId = null;
//         }}

//     @Test
//     void testGetPhysicians() {
//         Response response = webTarget
//             .register(adminAuth)
//             .path("physician")
//             .request()
//             .get();

//         assertEquals(200, response.getStatus());

//         List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>() {});
//         assertNotNull(physicians);
//         assertThat(physicians, is(not(empty())));
//     }

//     @Test
//     void testAddPhysician() {
//         Physician newPhysician = new Physician();
//         newPhysician.setFirstName("Jane");
//         newPhysician.setLastName("Doe");
    
//         Response response = webTarget
//             .register(adminAuth)
//             .path("physician")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
    
//         assertEquals(200, response.getStatus());
    
//         Physician created = response.readEntity(Physician.class);
//         assertNotNull(created.getId());
//         assertEquals("Jane", created.getFirstName());
//             webTarget
//             .register(adminAuth)
//             .path("physician/" + created.getId())
//             .request()
//             .delete();
//     }
    

//     @Test
//     void testGetPhysicianById() {
//         Response getResponse = webTarget
//             .register(adminAuth)
//             .path("physician/" + createdPhysicianId)
//             .request()
//             .get();

//         assertEquals(200, getResponse.getStatus());
//         Physician fetched = getResponse.readEntity(Physician.class);
//         assertEquals("test", fetched.getFirstName());
//     }

//     @Test
//     void testUpdateMedicineForPhysicianPatient() {
//         // 사전에 유효한 physicianId 와 patientId가 DB에 있어야 합니다
//         int physicianId = 1;
//         int patientId = 1;
//         Medicine medicine = new Medicine();
//         medicine.setDrugName("Aspirin");
//         medicine.setManufacturerName("McNeil Laboratories");
//         medicine.setDosageInformation("Take 2 tablets per day");
//         Response response = webTarget
//             .register(adminAuth)
//             .path("physician/" + physicianId + "/patient/" + patientId + "/medicine")
//             .request(MediaType.APPLICATION_JSON)
//             .put(Entity.entity(medicine, MediaType.APPLICATION_JSON));

//         assertEquals(200, response.getStatus());
//         Medicine updated = response.readEntity(Medicine.class);
//         assertEquals("Aspirin", updated.getDrugName());
//     }
//     @Test
// void testDeletePhysicianById() {
//     // Delete the physician created in @BeforeEach
//     Response deleteResponse = webTarget
//         .register(adminAuth)
//         .path("physician/" + createdPhysicianId)
//         .request()
//         .delete();

//     assertEquals(204, deleteResponse.getStatus()); // noContent()

//     Response getResponse = webTarget
//         .register(adminAuth)
//         .path("physician/" + createdPhysicianId)
//         .request()
//         .get();

//     assertEquals(404, getResponse.getStatus());

//     createdPhysicianId = null;
// }

// }
