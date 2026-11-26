package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
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
import acmemedical.entity.Prescription;

@Path("/prescription")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrescriptionResource {
    
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getAllPrescriptions() {
        LOG.debug("retrieving all prescriptions ...");
        List<Prescription> prescriptions = service.getAllPrescriptions();
        return Response.ok(prescriptions).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{patientId}/{physicianId}")
    public Response getPrescriptionById(
            @PathParam("patientId") int patientId,
            @PathParam("physicianId") int physicianId) {
        LOG.debug("retrieving prescription for patient {} and physician {}", patientId, physicianId);
        Prescription prescription = service.getPrescriptionById(patientId, physicianId);
        return Response.ok(prescription).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response createPrescription(Prescription newPrescription) {
        LOG.debug("Creating new prescription");
        if (newPrescription == null) {
            return Response.status(Status.BAD_REQUEST).entity("Prescription data cannot be null").build();
        }
        
        Prescription prescription = service.persistPrescription(newPrescription);
        if (prescription == null) {
            return Response.status(Status.BAD_REQUEST)
                         .entity("Invalid prescription data: Make sure patient, physician, and medicine exist")
                         .build();
        }
        
        return Response.status(Status.CREATED).entity(prescription).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{patientId}/{physicianId}")
    public Response updatePrescription(
            @PathParam("patientId") int patientId,
            @PathParam("physicianId") int physicianId,
            Prescription updatingPrescription) {
        LOG.debug("Updating prescription for patient {} and physician {}", patientId, physicianId);
        Prescription updated = service.updatePrescription(patientId, physicianId, updatingPrescription);
        return Response.ok(updated).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{patientId}/{physicianId}")
    public Response deletePrescription(
            @PathParam("patientId") int patientId,
            @PathParam("physicianId") int physicianId) {
        LOG.debug("Deleting prescription for patient {} and physician {}", patientId, physicianId);
        service.deletePrescription(patientId, physicianId);
        return Response.noContent().build();
    }
} 