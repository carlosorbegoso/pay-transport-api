package com.skyblue.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"driver_id", "ticket_number", "timestamp"})
    })
public class Transaction extends PanacheEntity {
  @ManyToOne
  @JoinColumn(name = "driver_id", nullable = false)
  public Driver driver;

  @Column(name = "ticket_number")
  public String ticketNumber;

  @Enumerated(EnumType.STRING)
  public TicketType ticketType;

  @Enumerated(EnumType.STRING)
  public PaymentMethod paymentMethod;

  public Double amount;
  public LocalDateTime timestamp;
  public boolean synced;
}
