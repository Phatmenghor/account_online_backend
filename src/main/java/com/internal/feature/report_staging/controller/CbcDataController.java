// CbcDataController.java
package com.internal.feature.report_staging.controller;

import com.internal.feature.report_staging.dto.filter.CbcFilterRequestDto;
import com.internal.feature.report_staging.dto.request.CbcDataRequestDto;
import com.internal.feature.report_staging.dto.response.CbcMainRecordResponseDto;
import com.internal.feature.report_staging.dto.response.DataLoadStatusDto;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.service.CbcDataService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/cbc-data")
@RequiredArgsConstructor
@Slf4j
public class CbcDataController {

    private final CbcDataService cbcDataService;

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> loadCbcData(@Valid @RequestBody CbcDataRequestDto request) {
        log.info("Received request to load CBC data for date range: {} to {}",
                request.getStartDate(), request.getEndDate());

        DataLoadStatusDto status = cbcDataService.loadCbcData(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Data loading initiated successfully");
        response.put("data", status);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/load-status")
    public ResponseEntity<Map<String, Object>> getLoadStatus() {
        log.info("Fetching current load status");

        DataLoadStatusDto status = cbcDataService.getLoadStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Load status retrieved successfully");
        response.put("data", status);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllCbcRecords() {
        log.info("Fetching all CBC records without pagination");

        List<CbcMainRecordResponseDto> records = cbcDataService.getAllCbcRecords();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CBC records retrieved successfully");
        response.put("data", records);
        response.put("total", records.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getCbcRecordsPaginated(@Valid @RequestBody CbcFilterRequestDto filterRequest) {
        log.info("Fetching CBC records with pagination - Page: {}, Size: {}",
                filterRequest.getPage(), filterRequest.getSize());

        PaginationResponse<CbcMainRecordResponseDto> paginatedRecords = cbcDataService.getCbcRecordsPaginated(filterRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CBC records retrieved successfully");
        response.put("data", paginatedRecords);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/by-id")
    public ResponseEntity<Map<String, Object>> getCbcRecordById(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        log.info("Fetching CBC record by ID: {}", id);

        if (id == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        CbcMainRecordResponseDto record = cbcDataService.getCbcRecordById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CBC record retrieved successfully");
        response.put("data", record);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCbcRecord(@Valid @RequestBody Map<String, Object> request) {
        Object idObj = request.get("id");
        Object updateDataObj = request.get("updateData");

        if (idObj == null || updateDataObj == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Both 'id' and 'updateData' are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Long id;
        try {
            id = Long.valueOf(idObj.toString());
        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid ID format");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Convert updateData to CbcUpdateRequestDto
        // This is a simplified approach - in production, you'd want proper JSON deserialization
        CbcUpdateRequestDto updateRequest = convertMapToUpdateRequest(updateDataObj);

        log.info("Updating CBC record with ID: {}", id);

        CbcMainRecordResponseDto updatedRecord = cbcDataService.updateCbcRecord(id, updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CBC record updated successfully");
        response.put("data", updatedRecord);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteCbcRecord(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        log.info("Deleting CBC record with ID: {}", id);

        if (id == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        cbcDataService.deleteCbcRecord(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CBC record deleted successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllCbcRecords() {
        log.info("Deleting all CBC records");

        cbcDataService.deleteAllCbcRecords();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All CBC records deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTotalRecordsCount() {
        log.info("Fetching total records count");

        long count = cbcDataService.getTotalRecordsCount();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Total records count retrieved successfully");

        Map<String, Object> countData = new HashMap<>();
        countData.put("totalRecords", count);
        response.put("data", countData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCbcRecords(@Valid @RequestBody CbcFilterRequestDto searchRequest) {
        log.info("Searching CBC records with filters");

        PaginationResponse<CbcMainRecordResponseDto> searchResults = cbcDataService.getCbcRecordsPaginated(searchRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Search completed successfully");
        response.put("data", searchResults);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Error in CBC data controller: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", e.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (e.getMessage().contains("required") || e.getMessage().contains("Invalid")) {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Helper method to convert Map to CbcUpdateRequestDto
     * Comprehensive field mapping for all update fields
     */
    private CbcUpdateRequestDto convertMapToUpdateRequest(Object updateDataObj) {
        CbcUpdateRequestDto updateRequest = new CbcUpdateRequestDto();

        if (updateDataObj instanceof Map) {
            Map<String, Object> updateMap = (Map<String, Object>) updateDataObj;

            // Main Record Fields
            setStringField(updateMap, "accountNumber", updateRequest::setAccountNumber);
            setStringField(updateMap, "creditorId", updateRequest::setCreditorId);
            setStringField(updateMap, "accountType", updateRequest::setAccountType);
            setStringField(updateMap, "asOfDate", updateRequest::setAsOfDate);
            setLocalDateField(updateMap, "requestStartDate", updateRequest::setRequestStartDate);
            setLocalDateField(updateMap, "requestEndDate", updateRequest::setRequestEndDate);
            setLocalDateField(updateMap, "processingDate", updateRequest::setProcessingDate);

            // Personal Info Fields
            setStringField(updateMap, "dateOfBirth", updateRequest::setDateOfBirth);
            setStringField(updateMap, "familyNameEnglish", updateRequest::setFamilyNameEnglish);
            setStringField(updateMap, "firstNameEnglish", updateRequest::setFirstNameEnglish);
            setStringField(updateMap, "secondNameEnglish", updateRequest::setSecondNameEnglish);
            setStringField(updateMap, "thirdNameEnglish", updateRequest::setThirdNameEnglish);
            setStringField(updateMap, "unformattedNameEnglish", updateRequest::setUnformattedNameEnglish);
            setStringField(updateMap, "mothersNameUnformattedEnglish", updateRequest::setMothersNameUnformattedEnglish);
            setStringField(updateMap, "familyNameKhmer", updateRequest::setFamilyNameKhmer);
            setStringField(updateMap, "firstNameKhmer", updateRequest::setFirstNameKhmer);
            setStringField(updateMap, "secondNameKhmer", updateRequest::setSecondNameKhmer);
            setStringField(updateMap, "thirdNameKhmer", updateRequest::setThirdNameKhmer);
            setStringField(updateMap, "unformattedNameKhmer", updateRequest::setUnformattedNameKhmer);
            setStringField(updateMap, "mothersNameUnformattedKhmer", updateRequest::setMothersNameUnformattedKhmer);
            setStringField(updateMap, "gender", updateRequest::setGender);
            setStringField(updateMap, "maritalStatus", updateRequest::setMaritalStatus);
            setStringField(updateMap, "nationalityCode", updateRequest::setNationalityCode);
            setStringField(updateMap, "taxpayerRegistrationNumber", updateRequest::setTaxpayerRegistrationNumber);
            setStringField(updateMap, "applicantType", updateRequest::setApplicantType);

            // ID Information Fields
            setStringField(updateMap, "idType1", updateRequest::setIdType1);
            setStringField(updateMap, "idNumber1", updateRequest::setIdNumber1);
            setStringField(updateMap, "idExpiryDate1", updateRequest::setIdExpiryDate1);
            setStringField(updateMap, "idType2", updateRequest::setIdType2);
            setStringField(updateMap, "idNumber2", updateRequest::setIdNumber2);
            setStringField(updateMap, "idExpiryDate2", updateRequest::setIdExpiryDate2);
            setStringField(updateMap, "idType3", updateRequest::setIdType3);
            setStringField(updateMap, "idNumber3", updateRequest::setIdNumber3);
            setStringField(updateMap, "idExpiryDate3", updateRequest::setIdExpiryDate3);

            // Address Information Fields
            setStringField(updateMap, "addressType1", updateRequest::setAddressType1);
            setStringField(updateMap, "province1", updateRequest::setProvince1);
            setStringField(updateMap, "district1", updateRequest::setDistrict1);
            setStringField(updateMap, "commune1", updateRequest::setCommune1);
            setStringField(updateMap, "village1", updateRequest::setVillage1);
            setStringField(updateMap, "address1Field1English", updateRequest::setAddress1Field1English);
            setStringField(updateMap, "address1Field2English", updateRequest::setAddress1Field2English);
            setStringField(updateMap, "address1Field1Khmer", updateRequest::setAddress1Field1Khmer);
            setStringField(updateMap, "address1Field2Khmer", updateRequest::setAddress1Field2Khmer);
            setStringField(updateMap, "city1English", updateRequest::setCity1English);
            setStringField(updateMap, "city1Khmer", updateRequest::setCity1Khmer);
            setStringField(updateMap, "country1", updateRequest::setCountry1);
            setStringField(updateMap, "postalCode1", updateRequest::setPostalCode1);

            // Contact Information Fields
            setStringField(updateMap, "emailAddress", updateRequest::setEmailAddress);
            setStringField(updateMap, "contactNumberType1", updateRequest::setContactNumberType1);
            setStringField(updateMap, "contactNumberCountryCode1", updateRequest::setContactNumberCountryCode1);
            setStringField(updateMap, "contactNumberArea1", updateRequest::setContactNumberArea1);
            setStringField(updateMap, "contactNumberNumber1", updateRequest::setContactNumberNumber1);
            setStringField(updateMap, "contactNumberExtension1", updateRequest::setContactNumberExtension1);

            // Employment Information Fields
            setStringField(updateMap, "employmentStatus1", updateRequest::setEmploymentStatus1);
            setStringField(updateMap, "employmentType1", updateRequest::setEmploymentType1);
            setStringField(updateMap, "employer1NameEnglish", updateRequest::setEmployer1NameEnglish);
            setStringField(updateMap, "employer1NameKhmer", updateRequest::setEmployer1NameKhmer);
            setStringField(updateMap, "economicSector1", updateRequest::setEconomicSector1);
            setStringField(updateMap, "businessType1", updateRequest::setBusinessType1);
            setStringField(updateMap, "occupation1English", updateRequest::setOccupation1English);
            setBigDecimalField(updateMap, "monthlyBasicSalaryIncome1", updateRequest::setMonthlyBasicSalaryIncome1);
            setBigDecimalField(updateMap, "totalMonthlySalaryIncome1", updateRequest::setTotalMonthlySalaryIncome1);

            // Security Information Fields
            setStringField(updateMap, "securityType1", updateRequest::setSecurityType1);
            setStringField(updateMap, "securityNumber1", updateRequest::setSecurityNumber1);
            setStringField(updateMap, "securityCurrency1", updateRequest::setSecurityCurrency1);
            setBigDecimalField(updateMap, "securityValue1", updateRequest::setSecurityValue1);
            setStringField(updateMap, "securityLocation1", updateRequest::setSecurityLocation1);
            setStringField(updateMap, "securityTypePrimary", updateRequest::setSecurityTypePrimary);

            // Loan Information Fields
            setStringField(updateMap, "loanTermType", updateRequest::setLoanTermType);
            setStringField(updateMap, "groupAccountReference", updateRequest::setGroupAccountReference);
            setStringField(updateMap, "dateIssued", updateRequest::setDateIssued);
            setStringField(updateMap, "productType", updateRequest::setProductType);
            setStringField(updateMap, "currency", updateRequest::setCurrency);
            setBigDecimalField(updateMap, "productLimitOriginalAmount", updateRequest::setProductLimitOriginalAmount);
            setStringField(updateMap, "productExpiryDate", updateRequest::setProductExpiryDate);
            setStringField(updateMap, "productStatus", updateRequest::setProductStatus);
            setStringField(updateMap, "restructuredLoan", updateRequest::setRestructuredLoan);
            setBigDecimalField(updateMap, "instalmentAmount", updateRequest::setInstalmentAmount);
            setStringField(updateMap, "paymentFrequency", updateRequest::setPaymentFrequency);
            setStringField(updateMap, "tenure", updateRequest::setTenure);
            setStringField(updateMap, "lastPaymentDate", updateRequest::setLastPaymentDate);
            setBigDecimalField(updateMap, "lastAmountPaid", updateRequest::setLastAmountPaid);
            setBigDecimalField(updateMap, "outstandingBalance", updateRequest::setOutstandingBalance);
            setBigDecimalField(updateMap, "pastDue", updateRequest::setPastDue);
            setStringField(updateMap, "nextPaymentDate", updateRequest::setNextPaymentDate);
            setStringField(updateMap, "paymentStatusCode", updateRequest::setPaymentStatusCode);
        }

        return updateRequest;
    }

    private void setStringField(Map<String, Object> map, String key, Consumer<String> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            setter.accept(map.get(key).toString());
        }
    }

    private void setLocalDateField(Map<String, Object> map, String key, Consumer<LocalDate> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            try {
                setter.accept(LocalDate.parse(map.get(key).toString()));
            } catch (Exception e) {
                log.warn("Failed to parse LocalDate for key {}: {}", key, e.getMessage());
            }
        }
    }

    private void setBigDecimalField(Map<String, Object> map, String key, Consumer<BigDecimal> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            try {
                setter.accept(new BigDecimal(map.get(key).toString()));
            } catch (Exception e) {
                log.warn("Failed to parse BigDecimal for key {}: {}", key, e.getMessage());
            }
        }
    }
}