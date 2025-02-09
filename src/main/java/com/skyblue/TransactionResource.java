package com.skyblue;

import com.skyblue.model.Transaction;
import com.skyblue.service.ErrorResponse;
import com.skyblue.service.TransactionService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {
  @Inject
  TransactionService service;

  @POST
  @Path("/sync/{driverId}")
  public Uni<Response> create(@PathParam("driverId") Long driverId, Transaction transaction) {
    return service.save(transaction, driverId)
        .onItem().transform(saved -> Response.ok(saved).build())
        .onFailure().recoverWithItem(throwable ->
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error processing transaction: " + throwable.getMessage()))
                .build()
        );
  }

  @POST
  @Path("/batch/{driverId}")
  public Multi<Transaction> createBatch(@PathParam("driverId") Long driverId, List<Transaction> transactions) {
    return service.saveAll(transactions, driverId);
  }

  @GET
  @Path("/stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Multi<Transaction> streamTransactions() {
    return service.streamAll();
  }
}
