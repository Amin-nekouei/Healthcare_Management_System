/********************************************************************************************************
 * File:  MedicalTrainingResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.MEDICAL_TRAINING_RESOURCE_NAME;

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
import acmemedical.entity.MedicalTraining;
import acmemedical.entity.MedicalSchool;

@Path(MEDICAL_TRAINING_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalTrainingResource {
    
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getMedicalTrainings() {
        LOG.debug("Retrieving all medical trainings...");
        Response response = Response.ok(service.getAll(MedicalTraining.class, MedicalTraining.ALL_MEDICAL_TRAININGS_QUERY_NAME)).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{medicalTrainingId}")
    public Response getMedicalTrainingById(@PathParam("medicalTrainingId") int medicalTrainingId) {
        LOG.debug("Retrieving medical training with id = {}", medicalTrainingId);
        MedicalTraining medicalTraining = service.getMedicalTrainingById(medicalTrainingId);
        Response response = Response.ok(medicalTraining).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{medicalTrainingId}")
    public Response deleteMedicalTraining(@PathParam("medicalTrainingId") int medicalTrainingId) {
        LOG.debug("Deleting medical training with id = {}", medicalTrainingId);
        MedicalTraining medicalTraining = service.getMedicalTrainingById(medicalTrainingId);
        if (medicalTraining != null) {
            service.deleteMedicalTraining(medicalTrainingId);
            return Response.ok(medicalTraining).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    @RolesAllowed({ADMIN_ROLE})
    @POST
    public Response addMedicalTraining(MedicalTraining newMedicalTraining) {
        LOG.debug("Adding a new medical training = {}", newMedicalTraining);
        
        // Check if medicalSchool is provided
        if (newMedicalTraining.getMedicalSchool() == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Medical school is required. Please provide medicalSchool with id and public fields.")
                    .build();
        }
        
        // Check if medicalSchool has id
        if (newMedicalTraining.getMedicalSchool().getId() == 0) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Medical school ID is required")
                    .build();
        }
        
        // Get the existing medical school
        MedicalSchool school = service.getMedicalSchoolById(newMedicalTraining.getMedicalSchool().getId());
        if (school == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Medical school not found with ID: " + newMedicalTraining.getMedicalSchool().getId())
                    .build();
        }
        
        // Create a new MedicalTraining and set its properties
        MedicalTraining medicalTraining = new MedicalTraining();
        medicalTraining.setDurationAndStatus(newMedicalTraining.getDurationAndStatus());
        
        // Set the managed medical school
        medicalTraining.setMedicalSchool(school);
        
        // Add the training to the school's collection
        school.getMedicalTrainings().add(medicalTraining);
        
        // Update the school to persist the new training
        service.updateMedicalSchool(school.getId(), school);
        
        return Response.ok(medicalTraining).build();
    }

    @RolesAllowed({ADMIN_ROLE})
    @PUT
    @Path("/{medicalTrainingId}")
    public Response updateMedicalTraining(@PathParam("medicalTrainingId") int mtId, MedicalTraining updatingMedicalTraining) {
        LOG.debug("Updating a specific medical training with id = {}", mtId);
        
        // Get the existing medical training
        MedicalTraining existingTraining = service.getMedicalTrainingById(mtId);
        if (existingTraining == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Medical training not found with ID: " + mtId)
                    .build();
        }
        
        // Update only the duration and status
        existingTraining.setDurationAndStatus(updatingMedicalTraining.getDurationAndStatus());
        
        // Update the medical training
        MedicalTraining updatedMedicalTraining = service.updateMedicalTraining(mtId, existingTraining);
        return Response.ok(updatedMedicalTraining).build();
    }
} 