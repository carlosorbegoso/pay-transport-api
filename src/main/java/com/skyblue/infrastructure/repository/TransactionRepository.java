package com.skyblue.infrastructure.repository;

import com.skyblue.model.Transaction;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

  public Uni<Transaction> persist(Transaction transaction) {
    transaction.id = null;
    return transaction.<Transaction>persist();
  }

  public Multi<Transaction> persistBatch(List<Transaction> transactions) {
   return Multi.createFrom().iterable(transactions)
       .onItem().transformToUniAndConcatenate( transaction -> {
         transaction.id = null;
         return transaction.<Transaction>persist();
       });
  }

  public Multi<Transaction> streamAll() {
    return findAll().list()
        .onItem().transformToMulti(list ->
            Multi.createFrom().iterable(list)
        );
  }

}