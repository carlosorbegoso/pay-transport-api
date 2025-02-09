package com.skyblue.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver extends PanacheEntity {
  @Column(unique = true)
  public String driverCode;

  public String firstName;
  public String lastName;

  @Column(unique = true)
  public String username;
  public String password;

  @OneToMany(mappedBy = "driver")
  public List<Transaction> transactions;

  @ManyToOne
  @JoinColumn(name = "bus_id")
  public Bus bus;
}