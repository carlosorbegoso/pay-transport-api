package com.skyblue.service;

import com.skyblue.model.Driver;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ApplicationScoped
public class TicketNumberGenerator {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

  public String generate(Driver driver) {
    return String.format("%s-%s-%s",
        driver.driverCode,
        LocalDateTime.now().format(DATE_FORMATTER),
        UUID.randomUUID().toString().substring(0, 8)
    );
  }
}