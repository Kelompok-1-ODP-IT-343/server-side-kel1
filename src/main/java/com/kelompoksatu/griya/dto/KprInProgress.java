package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KprInProgress {
  private Integer id;
  private String applicantName;
  private String applicantEmail;
  private String applicantPhone;
  private String aplikasiKode;
  private String namaProperti;
  private String alamat;
  private BigDecimal harga;
  private String tanggal;
  private String jenis;
}
