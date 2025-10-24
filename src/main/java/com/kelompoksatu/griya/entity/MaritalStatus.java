package com.kelompoksatu.griya.entity;

/** Enum for marital status */
public enum MaritalStatus {
  SINGLE,
  MARRIED,
  DIVORCED,
  WIDOWED;

  public static MaritalStatus fromString(String maritalStatus) {
    return MaritalStatus.valueOf(maritalStatus.toUpperCase());
  }
}
