package com.skyblue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "transactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"driver_id", "ticket_number", "timestamp"})
    })
public class Transaction extends PanacheEntity {
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "driver_id", nullable = false)
  @JsonIgnoreProperties({"transactions"})
  public Driver driver;
  @Column(name = "ticket_number", unique = true)
  public String ticketNumber;

  @Enumerated(EnumType.STRING)
  public TicketType ticketType;

  @Enumerated(EnumType.STRING)
  public PaymentMethod paymentMethod;

  public Double amount;

  @Column(nullable = false)
  public LocalDateTime timestamp;

  public boolean synced;
}