package com.skyblue.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "buses")
public class Bus extends PanacheEntity {
  @Column(unique = true)
  public String plateNumber;

  public String route;

  @OneToMany(mappedBy = "bus")
  public List<Driver> drivers;
}