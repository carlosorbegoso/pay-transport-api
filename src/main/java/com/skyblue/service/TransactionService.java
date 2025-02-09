package com.skyblue.service;

import com.skyblue.infrastructure.repository.TransactionRepository;
import com.skyblue.model.Driver;
import com.skyblue.model.Transaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionService {
  @Inject
  TransactionRepository transactionRepository;

  private static final int BATCH_SIZE = 50;

  public Uni<Transaction> save(Transaction transaction, Long driverId) {
    return Driver.<Driver>findById(driverId)
        .onItem().ifNull().failWith(() ->
            new IllegalArgumentException("Driver not found"))
        .onItem().transform(driver -> {
          transaction.driver = driver;
          transaction.id = null;
          if (transaction.timestamp == null) {
            transaction.timestamp = LocalDateTime.now();
          }
          transaction.ticketNumber = generateTicketNumber(driver);
          return transaction;
        })
        .chain(preparedTransaction ->
            transactionRepository.persist(preparedTransaction));
  }

  private String generateTicketNumber(Driver driver) {
    return String.format("%s-%s-%s",
        driver.driverCode,
        LocalDateTime.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
        UUID.randomUUID().toString().substring(0, 8)
    );
  }

  public Multi<Transaction> saveAll(List<Transaction> transactions, Long driverId) {
    return Driver.<Driver>findById(driverId)
        .onItem().ifNull().failWith(() ->
            new IllegalArgumentException("Driver not found"))
        .onItem().transformToMulti(driver ->
            Multi.createFrom().iterable(transactions)
                .onItem().transform(transaction -> {
                  transaction.driver = driver;
                  transaction.id = null;
                  if (transaction.timestamp == null) {
                    transaction.timestamp = LocalDateTime.now();
                  }
                  transaction.ticketNumber = generateTicketNumber(driver);
                  return transaction;
                })
                .group().intoLists().of(BATCH_SIZE)
                .flatMap(batch ->
                    transactionRepository.persistBatch(batch)
                        .onItem().transformToMulti(list ->
                            Multi.createFrom().iterable(list))
                )
        );
  }


  private void prepareTransaction(Transaction transaction, LocalDateTime defaultTimestamp) {
    transaction.id = null;
    if (transaction.timestamp == null) {
      transaction.timestamp = defaultTimestamp;
    }
    if (!transaction.synced) {
      transaction.synced = false;
    }
  }
  public Multi<Transaction> streamAll() {
    return transactionRepository.streamAll()
        .onFailure().recoverWithCompletion();
  }

  public Uni<List<Transaction>> findUnsynced() {
    return transactionRepository.findUnsynced()
        .onFailure().recoverWithItem(List.of());
  }

  public Multi<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return Multi.createFrom().failure(
          new IllegalArgumentException("Start and end dates cannot be null")
      );
    }
    if (start.isAfter(end)) {
      return Multi.createFrom().failure(
          new IllegalArgumentException("Start date must be before end date")
      );
    }

    return transactionRepository.streamByDateRange(start, end)
        .onFailure().recoverWithCompletion();
  }

  public Uni<Integer> markTransactionsAsSynced(List<Long> transactionIds) {
    if (transactionIds == null || transactionIds.isEmpty()) {
      return Uni.createFrom().item(0);
    }

    return transactionRepository.markAsSynced(transactionIds)
        .onFailure().recoverWithItem(0);
  }

  public Uni<List<Transaction>> findByTicketType(String ticketType) {
    if (ticketType == null || ticketType.trim().isEmpty()) {
      return Uni.createFrom().item(List.of());
    }

    return transactionRepository.findByTicketType(ticketType)
        .onFailure().recoverWithItem(List.of());
  }
}