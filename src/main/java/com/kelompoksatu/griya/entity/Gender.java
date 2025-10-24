package com.kelompoksatu.griya.entity;

/** Enum for gender */
public enum Gender {
  MALE,
  FEMALE;

  public static Gender fromString(String gender) {
    return Gender.valueOf(gender.toUpperCase());
  }
}
