package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.request.AllAccountOnlineSuccessRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.AllAccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.mapper.AccountOnlineFinalMapper;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.logs_report.model.AccountOnlineOpenFinalAudit;
import com.internal.feature.logs_report.repository.AccountOnlineFinalAuditRepository;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.logs_report.specification.AccountOnlineFinalSpecification;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOnlineOpenFinalServiceImpl implements AccountOnlineOpenFinalService {

    private final AccountOnlineFinalRepository accountOnlineFinalRepository;
    private final MasterDataService masterDataService;
    private final AccountOnlineFinalAuditRepository onlineFinalAuditRepository;
    private final AccountOnlineFinalMapper mapper;
    private final SecurityUtils securityUtils;
    private final CustomerImageService customerImageService;

    @Override
    public AccountOnlineFinal saveFinalLog(
            CustomerRequest request,
            CustomerResponse accountInfo,
            AmlStatusDto amlProcessResult,
            CustomerImageUploadResponseDto imagePaths
    ) {
        try {
            LocalDate dob = parseDate(request.getDateOfBirth());
            LocalDate issueDate = parseDate(request.getLegalIssueDate());
            LocalDate expireDate = parseDate(request.getLegalExpireDate());

            // === Fetch master data ===
            ClsProvinceDto province = safeProvinceLookup(request.getCustomerCurrentProvince());
            ClsDistrictDto district = safeDistrictLookup(request.getCustomerCurrentDistrict());
            ClsCommuneDto commune = safeCommuneLookup(request.getCustomerCurrentCommune());
            ClsVillageDto village = safeVillageLookup(request.getCustomerCurrentVillage());

            ClsProvinceDto pobProvince = safeProvinceLookup(request.getCustomerPobProvince());
            ClsDistrictDto pobDistrict = safeDistrictLookup(request.getCustomerPobDistrict());
            ClsCommuneDto pobCommune = safeCommuneLookup(request.getCustomerPobCommune());
            ClsVillageDto pobVillage = safeVillageLookup(request.getCustomerPobVillage());

            ClsBranchDto branch = safeBranchLookup(request.getBranchCode());
            assert branch != null;

            // === Build entity ===
            AccountOnlineFinal finalLog = AccountOnlineFinal.builder()
                    // Legal
                    .legalId(request.getLegalId())
                    .legalDocName(request.getLegalDocType() != null ? request.getLegalDocType() : "NATIONAL.ID")
                    .legalHolderName(request.getGivenName() + " " + request.getFamilyName())
                    .legalFirstNameEn(request.getGivenName())
                    .legalLastNameEn(request.getFamilyName())
                    .legalFirstNameKh(request.getFirstNameKh())
                    .legalLastNameKh(request.getLastNameKh())
                    .legalDateOfBirth(dob)
                    .legalGender(request.getGender())
                    .legalAddress(request.getLegalAddress())
                    .legalPlaceOfBirth(request.getPlaceOfBirth())
                    .legalIssuedDate(issueDate)
                    .legalExpiredDate(expireDate)
                    .legalMRZ1(request.getLegalMrz1())
                    .legalMRZ2(request.getLegalMrz2())
                    .legalMRZ3(request.getLegalMrz3())

                    // Customer
                    .maritalStatus(request.getMaritalStatus())
                    .nationality("KH")
                    .companyName(request.getCompanyName())
                    .occupation(request.getOccupation())
                    .averageIncome("0")
                    .referralId(request.getReferralId())
                    .releasedBy("")

                    // Branch
                    .branchCode(request.getBranchCode())
                    .branchNameKh(branch.getBranchkh())

                    // Current address
                    .customerProvinceCode(request.getCustomerCurrentProvince())
                    .customerProvince(province != null ? province.getProvinceEn() + " / " + province.getProvinceKh() : null)
                    .customerDistrictCode(request.getCustomerCurrentDistrict())
                    .customerDistrict(district != null ? district.getDistrictEn() + " / " + district.getDistrictKh() : null)
                    .customerCommuneCode(request.getCustomerCurrentCommune())
                    .customerCommune(commune != null ? commune.getCommuneEn() + " / " + commune.getCommuneKh() : null)
                    .customerVillageCode(request.getCustomerCurrentVillage())
                    .customerVillage(village != null ? village.getVillageEn() + " / " + village.getVillageKh() : null)

                    // Place of birth
                    .customerPobProvinceCode(request.getCustomerPobProvince())
                    .customerPobProvince(pobProvince != null ? pobProvince.getProvinceEn() + " / " + pobProvince.getProvinceKh() : null)
                    .customerPobDistrictCode(request.getCustomerPobDistrict())
                    .customerPobDistrict(pobDistrict != null ? pobDistrict.getDistrictEn() + " / " + pobDistrict.getDistrictKh() : null)
                    .customerPobCommuneCode(request.getCustomerPobCommune())
                    .customerPobCommune(pobCommune != null ? pobCommune.getCommuneEn() + " / " + pobCommune.getCommuneKh() : null)
                    .customerPobVillageCode(request.getCustomerPobVillage())
                    .customerPobVillage(pobVillage != null ? pobVillage.getVillageEn() + " / " + pobVillage.getVillageKh() : null)

                    // Contact
                    .phoneNumber(request.getPhoneNumber())

                    // AML
                    .amlStatus(amlProcessResult.getStatus())
                    .amlActionBy(amlProcessResult.getApprovedBy() != null ? amlProcessResult.getApprovedBy().getId() :
                            amlProcessResult.getRejectedBy() != null ? amlProcessResult.getRejectedBy().getId() : null)
                    .amlActionName(amlProcessResult.getApprovedBy() != null ? amlProcessResult.getApprovedBy().getFullName() :
                            amlProcessResult.getRejectedBy() != null ? amlProcessResult.getRejectedBy().getFullName() : null)
                    .amlActionRole(amlProcessResult.getApprovedBy() != null ? amlProcessResult.getApprovedBy().getUserRole() :
                            amlProcessResult.getRejectedBy() != null ? amlProcessResult.getRejectedBy().getUserRole() : null)
                    .amlRemarks("")
                    .amlScreeningResult(amlProcessResult.getScreeningResult())
                    .amlRiskLevel(amlProcessResult.getRiskLevel())
                    .amlActionTaken(amlProcessResult.getActionTaken())
                    .amlTotalRulesScore(amlProcessResult.getTotalRulesScore())
                    .serviceName(amlProcessResult.getServiceName())
                    .amlTrxnId(amlProcessResult.getTrxnID())
                    .amlRulesTriggered(amlProcessResult.getRulesTriggered())

                    // Account info
                    .mnemonic(accountInfo.getMnemonic())
                    .usdAccount(accountInfo.getUsdAccount())
                    .khrAccount(accountInfo.getKhrAccount())
                    .cif(accountInfo.getCif())

                    // Images
                    .nidImage(imagePaths != null ? imagePaths.getNidImagePath() : request.getNidImage())
                    .selfieImage(imagePaths != null ? imagePaths.getSelfieImagePath() : request.getSelfieImage())
                    .build();

            accountOnlineFinalRepository.save(finalLog);
            log.info("✔ AccountOnlineFinal saved successfully for Legal ID: {}", request.getLegalId());
            return finalLog;

        } catch (Exception e) {
            log.error("✘ Failed to save AccountOnlineFinal for Legal ID {}: {}", request.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save AccountOnlineFinal", e);
        }
    }

    @Override
    public AllAccountOnlineFinalResponseDto getSuccessOpenAccount(AllAccountOnlineSuccessRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        var spec = AccountOnlineFinalSpecification.searchByName(request.getSearch());

        Page<AccountOnlineFinal> page = accountOnlineFinalRepository.findAll(spec, pageable);

        List<AccountOnlineFinalResponseDto> content = page.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return mapper.mapToListDto(content, page);
    }

    @Transactional
    @Override
    public void updateFinalLogWithAml(AmlStatusDto amlStatus) {
        Optional<AccountOnlineFinal> optionalFinalLog = accountOnlineFinalRepository
                .findByLegalId(amlStatus.getCustomerInfo().getLegalId());

        if (optionalFinalLog.isPresent()) {
            AccountOnlineFinal finalLog = optionalFinalLog.get();
            finalLog.setAmlStatus(amlStatus.getStatus());
            finalLog.setAmlActionBy(amlStatus.getApprovedBy() != null ? amlStatus.getApprovedBy().getId() :
                    amlStatus.getRejectedBy() != null ? amlStatus.getRejectedBy().getId() : null);
            finalLog.setAmlActionName(amlStatus.getApprovedBy() != null ? amlStatus.getApprovedBy().getFullName() :
                    amlStatus.getRejectedBy() != null ? amlStatus.getRejectedBy().getFullName() : null);
            finalLog.setAmlActionRole(amlStatus.getApprovedBy() != null ? amlStatus.getApprovedBy().getUserRole() :
                    amlStatus.getRejectedBy() != null ? amlStatus.getRejectedBy().getUserRole() : null);
            finalLog.setAmlRemarks(amlStatus.getRemarks());
            accountOnlineFinalRepository.save(finalLog);

            log.info("✔ AML updated for Legal ID: {}", amlStatus.getCustomerInfo().getLegalId());
        } else {
            log.warn("⚠ AML update skipped: AccountOnlineFinal not found for Legal ID {}",
                    amlStatus.getCustomerInfo().getLegalId());
        }
    }

    @Override
    public AccountOnlineFinalResponseDto findAccountByCifOrLegalId(AccountOnlineFinalLogRequestDto requestDto) {
        AccountOnlineFinal onlineFinal = accountOnlineFinalRepository
                .findTopByCifOrLegalIdOrderByCreatedAtDesc(requestDto.getCif(), requestDto.getLegalId())
                .orElseThrow(() -> new NotFoundException(
                        "Account not found for CIF: " + requestDto.getCif() + " or Legal ID: " + requestDto.getLegalId()
                ));

        AccountOnlineOpenFinalAudit audit = new AccountOnlineOpenFinalAudit();
        audit.setCif(requestDto.getCif());
        audit.setLegalId(requestDto.getLegalId());
        audit.setUser(securityUtils.getCurrentUser());
        audit.setAccount(onlineFinal);

        onlineFinalAuditRepository.save(audit);
        log.info("Saved account access audit for CIF: {} and Legal ID: {}", requestDto.getCif(), requestDto.getLegalId());

        
        AccountOnlineFinalResponseDto responseDto = mapper.toDto(onlineFinal);

            // Populate images
        try {
            String legalId = onlineFinal.getLegalId();
            log.info("Attempting to fetch images for Legal ID: {}", legalId);
            
            if (legalId != null) {
                byte[] nidBytes = customerImageService.getNidImageBytes(legalId);
                if (nidBytes != null) {
                    log.info("Found NID bytes: {} bytes", nidBytes.length);
                    responseDto.setNidImage(Base64.getEncoder().encodeToString(nidBytes));
                } else {
                    log.warn("NID bytes are NULL for Legal ID: {}", legalId);
                }

                byte[] selfieBytes = customerImageService.getSelfieImageBytes(legalId);
                if (selfieBytes != null) {
                    log.info("Found Selfie bytes: {} bytes", selfieBytes.length);
                    responseDto.setSelfieImage(Base64.getEncoder().encodeToString(selfieBytes));
                } else {
                    log.warn("Selfie bytes are NULL for Legal ID: {}", legalId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load images for Legal ID {}: {}", onlineFinal.getLegalId(), e.getMessage());
            // Continue without images
        }
        // End populate images

        return responseDto;
    }

    // === Helper methods ===
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            log.warn("⚠ Could not parse date: {}", dateStr);
            return null;
        }
    }

    private ClsProvinceDto safeProvinceLookup(String code) {
        try { return code != null ? masterDataService.getProvinceByCode(code) : null; }
        catch (Exception e) { log.warn("⚠ Province lookup failed for code {}", code); return null; }
    }

    private ClsDistrictDto safeDistrictLookup(String code) {
        try { return code != null ? masterDataService.getDistrictByCode(code) : null; }
        catch (Exception e) { log.warn("⚠ District lookup failed for code {}", code); return null; }
    }

    private ClsCommuneDto safeCommuneLookup(String code) {
        try { return code != null ? masterDataService.getCommuneByCode(code) : null; }
        catch (Exception e) { log.warn("⚠ Commune lookup failed for code {}", code); return null; }
    }

    private ClsVillageDto safeVillageLookup(String code) {
        try { return code != null ? masterDataService.getVillageByCode(code) : null; }
        catch (Exception e) { log.warn("⚠ Village lookup failed for code {}", code); return null; }
    }

    private ClsBranchDto safeBranchLookup(String code) {
        try { return code != null ? masterDataService.getBranchByCode(code) : null; }
        catch (Exception e) { log.warn("⚠ Branch lookup failed for code {}", code); return null; }
    }
}
