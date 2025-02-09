package com.skyblue.infrastructure.repository;

import com.skyblue.model.Transaction;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

  public Uni<Transaction> persist(Transaction transaction) {
    transaction.id = null;
    return transaction.<Transaction>persist();
  }

  public Uni<List<Transaction>> persistBatch(List<Transaction> transactions) {
    return getSession()
        .chain(session -> {

          return Multi.createFrom().iterable(transactions)
              .onItem().transform(transaction -> {
                transaction.id = null;
                return transaction;
              })
              .onItem().transformToUniAndConcatenate(transaction ->
                  transaction.<Transaction>persist()
              )
              .collect().asList()
              .call(() -> session.flush());
        });
  }

  public Uni<List<Transaction>> findUnsynced() {
    return find("synced = false").list();
  }

  public Multi<Transaction> streamAll() {
    return findAll().list()
        .onItem().transformToMulti(list ->
            Multi.createFrom().iterable(list)
        );
  }

  public Uni<List<Transaction>> findByDateRange(LocalDateTime start, LocalDateTime end) {
    return find("timestamp between ?1 and ?2", start, end).list();
  }

  public Multi<Transaction> streamByDateRange(LocalDateTime start, LocalDateTime end) {
    return find("timestamp between ?1 and ?2", start, end)
        .list()
        .onItem().transformToMulti(list ->
            Multi.createFrom().iterable(list)
        );
  }

  public Uni<List<Transaction>> findByTicketType(String ticketType) {
    return find("ticketType", ticketType).list();
  }

  public Uni<Integer> markAsSynced(List<Long> ids) {
    return update("synced = true where id in (?1)", ids);
  }
}