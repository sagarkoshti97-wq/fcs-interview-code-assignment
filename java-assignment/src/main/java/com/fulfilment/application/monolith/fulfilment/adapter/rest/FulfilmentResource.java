package com.fulfilment.application.monolith.fulfilment.adapter.rest;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.AssignFulfilmentUseCase;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.ports.RemoveFulfilmentUseCase;
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

  @Inject FulfilmentStore fulfilmentStore;

  @Inject AssignFulfilmentUseCase assignFulfilmentUseCase;

  @Inject RemoveFulfilmentUseCase removeFulfilmentUseCase;

  @GET
  public List<FulfilmentAssociation> listAll() {
    return fulfilmentStore.findAll();
  }

  @POST
  public Response assign(FulfilmentAssociation request) {
    FulfilmentAssociation created =
        assignFulfilmentUseCase.assign(
            request != null ? request.warehouseBusinessUnitCode : null,
            request != null ? request.storeId : null,
            request != null ? request.productId : null);
    return Response.status(201).entity(created).build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    removeFulfilmentUseCase.remove(id);
    return Response.status(204).build();
  }
}