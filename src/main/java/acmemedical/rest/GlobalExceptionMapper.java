package acmemedical.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Unhandled server error", exception);  // ✅ Logs full stack trace
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Server Error: " + exception.getMessage())
                .type("text/plain")
                .build();
    }
}
