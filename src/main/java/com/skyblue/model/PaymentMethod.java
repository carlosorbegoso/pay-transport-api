package com.skyblue.model;

public enum PaymentMethod {
  CASH("Cash"),
  CARD("Card"),
  MOBILE("Mobile Payment");

  private final String displayName;

  PaymentMethod(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}