package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import lombok.*;

/** Response DTO for successful KPR application submission */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KprHistoryListResponse {
  // Return Nama rumah, Lokasi Rumah, Aplikasi Kode, Jumlah Pinjaman,  Tanggal Pengajuan, Foto
  // Properti
  private String namaRumah;
  private String statusPengajuan;
  private String lokasiRumah;
  private String aplikasiKode;
  private BigDecimal jumlahPinjaman;
  private String tanggalPengajuan;
  private String fotoProperti;
}
