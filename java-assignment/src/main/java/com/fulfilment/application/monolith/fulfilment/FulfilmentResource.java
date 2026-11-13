package com.fulfilment.application.monolith.fulfilment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentResource {

  @Inject FulfilmentService fulfilmentService;

  @GET
  public List<FulfilmentAssociation> listAll() {
    return fulfilmentService.listAll();
  }

  @POST
  public Response assign(FulfilmentAssociation request) {
    FulfilmentAssociation created =
        fulfilmentService.assign(
            request != null ? request.warehouseBusinessUnitCode : null,
            request != null ? request.storeId : null,
            request != null ? request.productId : null);
    return Response.status(201).entity(created).build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    fulfilmentService.remove(id);
    return Response.status(204).build();
  }
}