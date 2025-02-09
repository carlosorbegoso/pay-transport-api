package com.skyblue;

import com.skyblue.model.Transaction;
import com.skyblue.service.ErrorResponse;
import com.skyblue.service.TransactionService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
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
  @WithSession
  public Uni<Response> createBatch(@PathParam("driverId") Long driverId, List<Transaction> transactions) {
    return service.saveAll(transactions, driverId)
        .collect().asList()
        .onItem().transform(savedTransactions ->
            Response.ok(savedTransactions).build())
        .onFailure().recoverWithItem(throwable ->
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error processing batch: " + throwable.getMessage()))
                .build()
        );
  }

  @GET
  @Path("/stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Multi<Transaction> streamTransactions() {
    return service.streamAll();
  }
}
