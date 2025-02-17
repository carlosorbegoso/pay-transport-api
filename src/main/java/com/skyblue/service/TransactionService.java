package com.skyblue.service;

import com.skyblue.DriverNotFoundException;
import com.skyblue.infrastructure.repository.TransactionRepository;
import com.skyblue.model.Driver;
import com.skyblue.model.Transaction;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionService {
  @Inject
  TransactionRepository transactionRepository;

  @Inject
  TicketNumberGenerator ticketNumberGenerator;

  @Inject
  Logger logger;


  @WithTransaction
  public Uni<Transaction> save(Transaction transaction, Long driverId) {
    return findDriverAndPrepareTransaction(driverId, transaction)
        .chain(this::persistTransaction);
  }

  private Uni<Transaction> findDriverAndPrepareTransaction(Long driverId, Transaction transaction) {
    return Driver.<Driver>findById(driverId)
        .onItem().ifNull().failWith(() -> new DriverNotFoundException(driverId))
        .map(driver -> prepareTransactionWithDriver(transaction, driver));
  }

  private Transaction prepareTransactionWithDriver(Transaction transaction, Driver driver) {
    transaction.driver = driver;
    transaction.id = null;
    transaction.timestamp = transaction.timestamp != null ?
        transaction.timestamp : LocalDateTime.now();
    transaction.ticketNumber = ticketNumberGenerator.generate(driver);
    return transaction;
  }
  private Uni<Transaction> persistTransaction(Transaction transaction) {
    logger.debug("Persisting transaction: " + transaction.ticketNumber);
    return transaction.<Transaction>persist()
        .onItem().invoke(saved -> logger.debug("Persisted transaction: " + saved.id))
        .onFailure().invoke(error -> logger.error("Failed to persist", error));
  }

  @WithTransaction
  public Uni<List<Transaction>> saveAll(List<Transaction> transactions, Long driverId) {
    return Driver.<Driver>findById(driverId)
        .onItem().ifNull().failWith(() -> new DriverNotFoundException(driverId))
        .onItem().transformToMulti(driver ->
            Multi.createFrom().iterable(transactions)
                .map(transaction -> prepareTransactionWithDriver(transaction, driver)))
        .collect().asList()
        .chain(preparedTransactions -> transactionRepository.persistBatch(preparedTransactions));
  }

}