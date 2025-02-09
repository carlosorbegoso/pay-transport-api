package com.skyblue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

  @JsonIgnore
  @OneToMany(mappedBy = "driver")
  public List<Transaction> transactions;

  @ManyToOne
  @JoinColumn(name = "bus_id")
  @JsonIgnoreProperties("drivers")
  public Bus bus;
}