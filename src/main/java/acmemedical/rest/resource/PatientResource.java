/********************************************************************************************************
 * File:  PatientResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmemedical.utility.MyConstants.PATIENT_RESOURCE_NAME;

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
import acmemedical.entity.Patient;
import acmemedical.entity.Prescription;

@Path(PATIENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {
    
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getPatients() {
        LOG.debug("retrieving all patients ...");
        List<Patient> patients = service.getAllPatients();
        Response response = Response.ok(patients).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getPatientById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific patient " + id);
        Patient patient = service.getPatientById(id);
        Response response = Response.status(patient == null ? Status.NOT_FOUND : Status.OK)
                                 .entity(patient)
                                 .build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPatient(Patient newPatient) {
        LOG.debug("Adding a new patient = {}", newPatient);
        Patient patientWithId = service.persistPatient(newPatient);
        Response response = Response.ok(patientWithId).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updatePatient(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Patient updatingPatient) {
        LOG.debug("Updating patient with id = {}", id);
        Patient updatedPatient = service.updatePatient(id, updatingPatient);
        Response response = Response.ok(updatedPatient).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePatient(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Deleting patient with id = {}", id);
        service.deletePatientById(id);
        Response response = Response.ok().build();
        return response;
    }

    @POST
@RolesAllowed({ADMIN_ROLE})
@Path("/{patientId}/prescription")
public Response addPrescriptionToPatient(@PathParam("patientId") int patientId, Prescription newPrescription) {
    LOG.debug("Adding a new prescription to patient with id = {}", patientId);

    // Set the patient explicitly (already done here)
    Patient patient = service.getPatientById(patientId);
    newPrescription.setPatient(patient);

    // Use the service method that handles composite keys and associations
    Prescription persisted = service.persistPrescription(newPrescription);
    
    if (persisted == null) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Prescription could not be added.").build();
    }

    return Response.ok(persisted).build();
}

}