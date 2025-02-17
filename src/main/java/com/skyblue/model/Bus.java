package com.skyblue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "buses")
public class Bus extends PanacheEntity {
  @Column(name = "plate_number", unique = true, nullable = false)
  public String plateNumber;

  @Column(name = "route", nullable = false)
  public String route;

  @JsonIgnore
  @OneToMany(mappedBy = "bus")
  public List<Driver> drivers;
}