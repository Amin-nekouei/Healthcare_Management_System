/********************************************************************************************************
 * File:  MedicineResource.java Course Materials CST 8277
 *
 * @author Your Name
 */
package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.MEDICINE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Medicine;

@Path(MEDICINE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicineResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMedicines() {
        LOG.debug("Retrieving all medicines...");
        List<Medicine> medicines = service.getAllMedicines();
        return Response.ok(medicines).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicineById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Retrieving medicine with ID: {}", id);
        Medicine medicine = service.getMedicineById(id);
        if (medicine == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(medicine).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicine(Medicine newMedicine) {
        LOG.debug("Adding new medicine...");
        Medicine persistedMedicine = service.persistMedicine(newMedicine);
        return Response.status(Status.CREATED).entity(persistedMedicine).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMedicine(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Medicine updatedMedicine) {
        LOG.debug("Updating medicine with ID: {}", id);
        Medicine existingMedicine = service.getMedicineById(id);
        if (existingMedicine == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        updatedMedicine.setId(id);
        Medicine saved = service.updateMedicine(updatedMedicine);
        return Response.ok(saved).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicine(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Deleting medicine with ID: {}", id);
        Medicine existingMedicine = service.getMedicineById(id);
        if (existingMedicine == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        service.deleteMedicineById(id);
        return Response.noContent().build();
    }
}
