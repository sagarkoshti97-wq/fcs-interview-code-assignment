package com.fulfilment.application.monolith.fulfilment.adapter.rest;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentConstraintException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentDomainException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class FulfilmentExceptionMapper implements ExceptionMapper<FulfilmentDomainException> {

  @Override
  public Response toResponse(FulfilmentDomainException exception) {
    int status = exception instanceof FulfilmentNotFoundException ? 404 : 400;
    if (!(exception instanceof FulfilmentNotFoundException)
        && !(exception instanceof FulfilmentConstraintException)) {
      status = 400;
    }
    return Response.status(status)
        .entity(Map.of("error", exception.getMessage()))
        .build();
  }
}