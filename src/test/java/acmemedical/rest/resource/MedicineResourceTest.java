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

// import jakarta.ws.rs.client.Client;
// import jakarta.ws.rs.client.ClientBuilder;
// import jakarta.ws.rs.client.Entity;
// import jakarta.ws.rs.client.WebTarget;
// import jakarta.ws.rs.core.GenericType;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;

// class MedicineResourceTest {

//     private Integer createdMedicineId;
//     private Medicine testMedicine;
//     private static WebTarget webTarget;
//     private static HttpAuthenticationFeature adminAuth;

//     private static final String BASE_URI = "http://localhost:8080/rest-acmemedical-0.0.1-SNAPSHOT/api/v1";

//     @BeforeAll
//     static void setUpBeforeClass() {
//         Client client = ClientBuilder.newClient();
//         webTarget = client.target(BASE_URI);
//         adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
//     }

//     @BeforeEach
//     void setUp() {
//         testMedicine = new Medicine();
//         testMedicine.setDrugName("TestDrug");
//         testMedicine.setManufacturerName("TestManufacturer");
//         testMedicine.setDosageInformation("Take once a day");

//         Response response = webTarget
//             .register(adminAuth)
//             .path("medicine")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(testMedicine, MediaType.APPLICATION_JSON));

//         assertEquals(201, response.getStatus());

//         Medicine created = response.readEntity(Medicine.class);
//         createdMedicineId = created.getId();
//     }

//     @AfterEach
//     void tearDown() {
//         if (createdMedicineId != null) {
//             Response response = webTarget
//                 .register(adminAuth)
//                 .path("medicine/" + createdMedicineId)
//                 .request()
//                 .delete();

//             System.out.println("Deleted Medicine ID: " + createdMedicineId + " (status " + response.getStatus() + ")");
//             createdMedicineId = null;
//         }
//     }

//     @Test
//     void testGetAllMedicines() {
//         Response response = webTarget
//             .register(adminAuth)
//             .path("medicine")
//             .request()
//             .get();

//         assertEquals(200, response.getStatus());

//         List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>() {});
//         assertNotNull(medicines);
//         assertThat(medicines, is(not(empty())));
//     }

//     @Test
//     void testGetMedicineById() {
//         Response getResponse = webTarget
//             .register(adminAuth)
//             .path("medicine/" + createdMedicineId)
//             .request()
//             .get();

//         assertEquals(200, getResponse.getStatus());
//         Medicine fetched = getResponse.readEntity(Medicine.class);
//         assertEquals("TestDrug", fetched.getDrugName());
//     }
//     @Test
//     void testAddMedicine() {
//         Medicine newMed = new Medicine();
//         newMed.setDrugName("Ibuprofen");
//         newMed.setManufacturerName("ACME Pharmaceuticals");
//         newMed.setDosageInformation("200mg twice daily");
    
//         Response response = webTarget
//             .register(adminAuth)
//             .path("medicine")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(newMed, MediaType.APPLICATION_JSON));
    
//         assertEquals(201, response.getStatus());
    
//         Medicine created = response.readEntity(Medicine.class);
//         assertNotNull(created.getId());
//         assertEquals("Ibuprofen", created.getDrugName());
    
//         // Cleanup: delete the created medicine
//         webTarget
//             .register(adminAuth)
//             .path("medicine/" + created.getId())
//             .request()
//             .delete();
//     }
    
//     @Test
//     void testUpdateMedicine() {
//         Medicine update = new Medicine();
//         update.setDrugName("UpdatedDrug");
//         update.setManufacturerName("UpdatedManufacturer");
//         update.setDosageInformation("Updated dosage");

//         Response putResponse = webTarget
//             .register(adminAuth)
//             .path("medicine/" + createdMedicineId)
//             .request(MediaType.APPLICATION_JSON)
//             .put(Entity.entity(update, MediaType.APPLICATION_JSON));

//         assertEquals(200, putResponse.getStatus());

//         Medicine updated = putResponse.readEntity(Medicine.class);
//         assertEquals("UpdatedDrug", updated.getDrugName());
//     }

//     @Test
//     void testDeleteMedicine() {
//         Response deleteResponse = webTarget
//             .register(adminAuth)
//             .path("medicine/" + createdMedicineId)
//             .request()
//             .delete();

//         assertEquals(204, deleteResponse.getStatus());

//         Response getResponse = webTarget
//             .register(adminAuth)
//             .path("medicine/" + createdMedicineId)
//             .request()
//             .get();

//         assertEquals(404, getResponse.getStatus());

//         createdMedicineId = null;
//     }
// }
