package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.util.IDCloudHostS3Util;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ImageProxyController {

  private final IDCloudHostS3Util s3Util;

  @GetMapping("/api/v1/proxy/image")
  public ResponseEntity<byte[]> proxy(
      @RequestParam("key") String key,
      @RequestParam("token") String token,
      @RequestParam(value = "exp", required = false) Long exp) {
    if (key == null || token == null) {
      return ResponseEntity.status(401).build();
    }
    long now = java.time.Instant.now().getEpochSecond();
    if (exp != null && exp < now) {
      return ResponseEntity.status(401).build();
    }
    String expected =
        org.apache.commons.codec.digest.DigestUtils.sha256Hex(
            key + ":" + (exp != null ? exp : now) + ":" + getProxySecret());
    if (!expected.equals(token)) {
      return ResponseEntity.status(401).build();
    }
    try (var stream = s3Util.getObjectStream(key)) {
      String contentType = stream.response().contentType();
      InputStream is = stream;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int r;
      while ((r = is.read(buf)) != -1) {
        bos.write(buf, 0, r);
      }
      byte[] data = bos.toByteArray();
      MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
      if (contentType != null) {
        try {
          mt = MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
        }
      }
      return ResponseEntity.ok().contentType(mt).body(data);
    } catch (Exception e) {
      return ResponseEntity.status(404).build();
    }
  }

  private String getProxySecret() {
    try {
      java.lang.reflect.Field f = IDCloudHostS3Util.class.getDeclaredField("proxySecret");
      f.setAccessible(true);
      Object v = f.get(s3Util);
      return v != null ? v.toString() : "";
    } catch (Exception e) {
      return "";
    }
  }
}
