package com.skyblue;

public class DriverNotFoundException extends RuntimeException {
  public DriverNotFoundException(Long driverId) {
    super("Driver not found with ID: " + driverId);
  }
}