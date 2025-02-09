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
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionService {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TransactionService.class);
  @Inject
  TransactionRepository transactionRepository;

  @Inject
  TicketNumberGenerator ticketNumberGenerator;

  private static final int BATCH_SIZE = 50;
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
  private Transaction prepareTransactionWithDriver(Transaction transaction, Driver driver){
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


   public Multi<Transaction> saveAll(List<Transaction> transactions, Long driverId){
    return Driver.<Driver>findById(driverId)
        .onItem().ifNull().failWith(() -> new DriverNotFoundException(driverId))
        .onItem().transformToMulti(driver -> processTransactions(transactions, driver));
   }


  private Multi<Transaction> processTransactions(List<Transaction> transactions, Driver driver) {
    return Multi.createFrom().iterable(transactions)
        .map(transaction -> prepareTransactionWithDriver(transaction, driver))
        .group().intoLists().of(BATCH_SIZE)
        .flatMap(transactionRepository::persistBatch);
  }

  public Multi<Transaction> streamAll() {
    return transactionRepository.streamAll()
        .onFailure().recoverWithCompletion();
  }

}