/********************************************************************************************************
 * File:  MedicalCertificateResource.java Course Materials CST 8277
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
import static acmemedical.utility.MyConstants.MEDICAL_CERTIFICATE_RESOURCE_NAME;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
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
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;

@Path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalCertificateResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMedicalCertificates() {
        LOG.debug("retrieving all medical certificates ...");
        List<MedicalCertificate> certificates = service.getAllMedicalCertificates();
        Response response = Response.ok(certificates).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicalCertificateById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific medical certificate " + id);
        Response response = null;
        MedicalCertificate certificate = null;

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            certificate = service.getMedicalCertificateById(id);
            response = Response.status(certificate == null ? Status.NOT_FOUND : Status.OK).entity(certificate).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Physician physician = sUser.getPhysician();
            certificate = service.getMedicalCertificateById(id);
            
            if (certificate != null && certificate.getOwner().getId() == physician.getId()) {
                response = Response.status(Status.OK).entity(certificate).build();
            } else {
                throw new ForbiddenException("User trying to access resource it does not own (wrong userid)");
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalCertificate(MedicalCertificate newMedicalCertificate) {
        LOG.debug("Adding a new medical certificate = {}", newMedicalCertificate);
        MedicalCertificate certificateWithId = service.persistMedicalCertificate(newMedicalCertificate);
        Response response = Response.ok(certificateWithId).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMedicalCertificate(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, MedicalCertificate updatingMedicalCertificate) {
        LOG.debug("Updating medical certificate with id = {}", id);
        System.out.println(">>> Received PUT: ID=" + id);
System.out.println(">>> Payload signed = " + updatingMedicalCertificate.getSigned());
System.out.println(">>> Payload owner = " + updatingMedicalCertificate.getOwner());

        MedicalCertificate updatedCertificate = service.updateMedicalCertificate(id, updatingMedicalCertificate);
        Response response = Response.ok(updatedCertificate).build();

        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicalCertificate(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Deleting medical certificate with id = {}", id);
        service.deleteMedicalCertificateById(id);
        Response response = Response.ok().build();
        return response;
    }
} 
