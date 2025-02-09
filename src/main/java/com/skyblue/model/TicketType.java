package com.skyblue.model;

public enum TicketType {
  ADULT_DIRECT("Direct", 2.00),
  ADULT_ZONAL("Zonal", 2.50),
  ADULT_INTERZONAL("Interzonal", 3.00),
  STUDENT_DIRECT("Direct", 1.00),
  STUDENT_ZONAL("Zonal", 1.20),
  STUDENT_INTERZONAL("Interzonal", 1.50);

  private final String label;
  private final double price;

  TicketType(String label, double price) {
    this.label = label;
    this.price = price;
  }

  public String getLabel() {
    return label;
  }

  public double getPrice() {
    return price;
  }

  public boolean isStudentTicket() {
    return this.name().startsWith("STUDENT_");
  }

  public boolean isAdultTicket() {
    return this.name().startsWith("ADULT_");
  }
}