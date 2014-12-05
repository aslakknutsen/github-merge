package org.aslak.github.merge.rest.jaxrs;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .entity(
                    new StringBuilder()
                        .append("{\n")
                            .append("\"message\": ").append(exception.getMessage()).append("\",")
                        .append("}\n")
                    )
            .build();
    }
}
