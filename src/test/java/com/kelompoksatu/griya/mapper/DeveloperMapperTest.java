package com.kelompoksatu.griya.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.kelompoksatu.griya.dto.CreateDeveloperRequest;
import com.kelompoksatu.griya.dto.UpdateDeveloperRequest;
import com.kelompoksatu.griya.entity.Developer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Test class for DeveloperMapper MapStruct implementation */
class DeveloperMapperTest {

  private final DeveloperMapper mapper = Mappers.getMapper(DeveloperMapper.class);

  @Test
  void testToEntity_CreateDeveloperRequest() {
    // Given
    CreateDeveloperRequest request = new CreateDeveloperRequest();
    request.setCompanyName("Test Company");
    request.setCompanyCode("TC001");
    request.setBusinessLicense("SIUP123");
    request.setDeveloperLicense("DEV456");
    request.setContactPerson("John Doe");
    request.setPhone("08123456789");
    request.setEmail("test@company.com");
    request.setWebsite("https://testcompany.com");
    request.setAddress("123 Test Street");
    request.setCity("Jakarta");
    request.setProvince("DKI Jakarta");
    request.setPostalCode("12345");
    request.setEstablishedYear(2020);
    request.setDescription("Test company description");
    request.setSpecialization(Developer.Specialization.RESIDENTIAL);
    request.setIsPartner(true);
    request.setPartnershipLevel(Developer.PartnershipLevel.GOLD);
    request.setCommissionRate(new BigDecimal("0.0300"));

    // When
    Developer developer = mapper.toEntity(request);

    // Then
    assertNotNull(developer);
    assertEquals("Test Company", developer.getCompanyName());
    assertEquals("TC001", developer.getCompanyCode());
    assertEquals("SIUP123", developer.getBusinessLicense());
    assertEquals("DEV456", developer.getDeveloperLicense());
    assertEquals("John Doe", developer.getContactPerson());
    assertEquals("08123456789", developer.getPhone());
    assertEquals("test@company.com", developer.getEmail());
    assertEquals("https://testcompany.com", developer.getWebsite());
    assertEquals("123 Test Street", developer.getAddress());
    assertEquals("Jakarta", developer.getCity());
    assertEquals("DKI Jakarta", developer.getProvince());
    assertEquals("12345", developer.getPostalCode());
    assertEquals(2020, developer.getEstablishedYear());
    assertEquals("Test company description", developer.getDescription());
    assertEquals(Developer.Specialization.RESIDENTIAL, developer.getSpecialization());
    assertTrue(developer.getIsPartner());
    assertEquals(Developer.PartnershipLevel.GOLD, developer.getPartnershipLevel());
    assertEquals(new BigDecimal("0.0300"), developer.getCommissionRate());

    // Verify ignored fields are null
    assertNull(developer.getId());
    assertNull(developer.getVerifiedAt());
    assertNull(developer.getVerifiedBy());
    assertNull(developer.getCreatedAt());
    assertNull(developer.getUpdatedAt());
    assertNull(developer.getUser()); // User relationship is set separately in service
  }

  @Test
  void testUpdateDeveloperFromRequest_PartialUpdate() {
    // Given
    Developer existingDeveloper = new Developer();
    existingDeveloper.setId(1);
    existingDeveloper.setCompanyName("Original Company");
    existingDeveloper.setEmail("original@company.com");
    existingDeveloper.setPhone("08111111111");
    existingDeveloper.setIsPartner(false);
    existingDeveloper.setStatus(Developer.DeveloperStatus.ACTIVE);

    UpdateDeveloperRequest request = new UpdateDeveloperRequest();
    request.setCompanyName("Updated Company");
    request.setPhone("08222222222");
    // Note: email and isPartner are null, so they should not be updated

    // When
    mapper.updateDeveloperFromRequest(request, existingDeveloper);

    // Then
    assertEquals(1, existingDeveloper.getId()); // Should remain unchanged
    assertEquals("Updated Company", existingDeveloper.getCompanyName()); // Should be updated
    assertEquals(
        "original@company.com",
        existingDeveloper.getEmail()); // Should remain unchanged (null in request)
    assertEquals("08222222222", existingDeveloper.getPhone()); // Should be updated
    assertFalse(existingDeveloper.getIsPartner()); // Should remain unchanged (null in request)
    assertEquals(
        Developer.DeveloperStatus.ACTIVE, existingDeveloper.getStatus()); // Should remain unchanged
  }

  @Test
  void testToResponse_Developer() {
    // Given
    Developer developer = new Developer();
    developer.setId(1);
    developer.setCompanyName("Test Company");
    developer.setCompanyCode("TC001");
    developer.setEmail("test@company.com");
    developer.setSpecialization(Developer.Specialization.COMMERCIAL);
    developer.setIsPartner(true);
    developer.setPartnershipLevel(Developer.PartnershipLevel.SILVER);
    developer.setCommissionRate(new BigDecimal("0.0250"));
    developer.setStatus(Developer.DeveloperStatus.ACTIVE);
    developer.setCreatedAt(LocalDateTime.now());
    developer.setUpdatedAt(LocalDateTime.now());

    // When
    var response = mapper.toResponse(developer);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getId());
    assertEquals("Test Company", response.getCompanyName());
    assertEquals("TC001", response.getCompanyCode());
    assertEquals("test@company.com", response.getEmail());
    assertEquals(Developer.Specialization.COMMERCIAL, response.getSpecialization());
    assertTrue(response.getIsPartner());
    assertEquals(Developer.PartnershipLevel.SILVER, response.getPartnershipLevel());
    assertEquals(new BigDecimal("0.0250"), response.getCommissionRate());
    assertEquals(Developer.DeveloperStatus.ACTIVE, response.getStatus());
    assertNotNull(response.getCreatedAt());
    assertNotNull(response.getUpdatedAt());
  }
}
