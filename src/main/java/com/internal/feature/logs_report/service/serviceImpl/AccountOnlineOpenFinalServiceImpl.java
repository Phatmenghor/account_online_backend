package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.mapper.AccountOnlineFinalMapper;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.logs_report.model.AccountOnlineOpenFinalAudit;
import com.internal.feature.logs_report.repository.AccountOnlineFinalAuditRepository;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOnlineOpenFinalServiceImpl implements AccountOnlineOpenFinalService {

    private final AccountOnlineFinalRepository accountOnlineFinalRepository;
    private final MasterDataService masterDataService;
    private final AccountOnlineFinalAuditRepository onlineFinalAuditRepository;
    private final AccountOnlineFinalMapper mapper;
    private final SecurityUtils securityUtils;

    @Override
    public AccountOnlineFinal saveFinalLog(
            CustomerRequest request,
            CustomerResponse accountInfo,
            AmlStatusDto amlProcessResult,
            CustomerImageUploadResponseDto imagePaths
    ) {
        try {
            // === Parse dates safely ===
            LocalDate dob = parseDate(request.getDateOfBirth());
            LocalDate issueDate = parseDate(request.getLegalIssueDate());
            LocalDate expireDate = parseDate(request.getLegalExpireDate());

            // === Fetch master data for address ===
            ClsProvinceDto province = safeProvinceLookup(request.getCustomerCurrentProvince());
            ClsDistrictDto district = safeDistrictLookup(request.getCustomerCurrentDistrict());
            ClsCommuneDto commune = safeCommuneLookup(request.getCustomerCurrentCommune());
            ClsVillageDto village = safeVillageLookup(request.getCustomerCurrentVillage());

            // === Fetch master data for place of birth ===
            ClsProvinceDto pobProvince = safeProvinceLookup(request.getCustomerPobProvince());
            ClsDistrictDto pobDistrict = safeDistrictLookup(request.getCustomerPobDistrict());
            ClsCommuneDto pobCommune = safeCommuneLookup(request.getCustomerPobCommune());
            ClsVillageDto pobVillage = safeVillageLookup(request.getCustomerPobVillage());

            // === Fetch branch ===
            ClsBranchDto branch = safeBranchLookup(request.getBranchCode());

            // === Build the entity ===
            assert branch != null;
            AccountOnlineFinal finalLog = AccountOnlineFinal.builder()
                    // === Legal Info ===
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

                    // === Customer Info ===
                    .maritalStatus(request.getMaritalStatus())
                    .nationality("KH")
                    .companyName(request.getCompanyName())
                    .occupation(request.getOccupation())
                    .averageIncome("0")
                    .referralId(request.getReferralId())
                    .releasedBy("")

                    // === Branch Info ===
                    .branchCode(request.getBranchCode())
                    .branchNameKh(branch.getBranchkh())

                    // === Current Address ===
                    .customerProvinceCode(request.getCustomerCurrentProvince())
                    .customerProvince(province != null ? province.getProvinceEn() + " / " + province.getProvinceKh() : null)
                    .customerDistrictCode(request.getCustomerCurrentDistrict())
                    .customerDistrict(district != null ? district.getDistrictEn() + " / " + district.getDistrictKh() : null)
                    .customerCommuneCode(request.getCustomerCurrentCommune())
                    .customerCommune(commune != null ? commune.getCommuneEn() + " / " + commune.getCommuneKh() : null)
                    .customerVillageCode(request.getCustomerCurrentVillage())
                    .customerVillage(village != null ? village.getVillageEn() + " / " + village.getVillageKh() : null)

                    // === Place of Birth (POB) ===
                    .customerPobProvinceCode(request.getCustomerPobProvince())
                    .customerPobProvince(pobProvince != null ? pobProvince.getProvinceEn() + " / " + pobProvince.getProvinceKh() : null)
                    .customerPobDistrictCode(request.getCustomerPobDistrict())
                    .customerPobDistrict(pobDistrict != null ? pobDistrict.getDistrictEn() + " / " + pobDistrict.getDistrictKh() : null)
                    .customerPobCommuneCode(request.getCustomerPobCommune())
                    .customerPobCommune(pobCommune != null ? pobCommune.getCommuneEn() + " / " + pobCommune.getCommuneKh() : null)
                    .customerPobVillageCode(request.getCustomerPobVillage())
                    .customerPobVillage(pobVillage != null ? pobVillage.getVillageEn() + " / " + pobVillage.getVillageKh() : null)

                    // === Contact ===
                    .phoneNumber(request.getPhoneNumber())

                    // === AML Info ===
                    .amlActionTaken(amlProcessResult.getActionTaken())
                    .amlRiskLevel(amlProcessResult.getRiskLevel())
                    .amlTrxnId(amlProcessResult.getTrxnID())
                    .amlRulesTriggered(amlProcessResult.getRulesTriggered())
                    .amlTotalRulesScore(amlProcessResult.getTotalRulesScore())
                    .serviceName(amlProcessResult.getServiceName())
                    .amlRejectedById(amlProcessResult.getRejectedBy() != null ? amlProcessResult.getRejectedBy().getId() : null)
                    .amlApprovedById(amlProcessResult.getApprovedBy() != null ? amlProcessResult.getApprovedBy().getId() : null)
                    .amlStatus(amlProcessResult.getStatus())
                    .amlScreeningResult(amlProcessResult.getScreeningResult())
                    .amlRemarks("")

                    // === Account Info ===
                    .mnemonic(accountInfo.getMnemonic())
                    .usdAccount(accountInfo.getUsdAccount())
                    .khrAccount(accountInfo.getKhrAccount())
                    .cif(accountInfo.getCif())

                    // === Images ===
                    .nidImage(imagePaths != null ? imagePaths.getNidImagePath() : request.getNidImage())
                    .selfieImage(imagePaths != null ? imagePaths.getSelfieImagePath() : request.getSelfieImage())
                    .build();

            accountOnlineFinalRepository.save(finalLog);

            log.info("✅ AccountOnlineFinal saved successfully for Legal ID: {}", request.getLegalId());
            return finalLog;

        } catch (Exception e) {
            log.error("❌ Failed to save AccountOnlineFinal for Legal ID {}: {}", request.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save AccountOnlineFinal", e);
        }
    }

    @Transactional
    @Override
    public AccountOnlineFinal updateFinalLogWithAml(AmlStatusDto amlStatus) {
        try {
            // Fetch existing record
            AccountOnlineFinal finalLog = accountOnlineFinalRepository
                    .findByLegalId(amlStatus.getLegalId())
                    .orElseThrow(() -> new RuntimeException(
                            "AccountOnlineFinal not found for Legal ID: " + amlStatus.getLegalId()
                    ));

            // Update approve/reject
            finalLog.setAmlRemarks(amlStatus.getRemarks());
            finalLog.setAmlApprovedById(amlStatus.getApprovedBy() != null ? amlStatus.getApprovedBy().getId() : null);
            finalLog.setAmlRejectedById(amlStatus.getRejectedBy() != null ? amlStatus.getRejectedBy().getId() : null);

            // Save updated record
            accountOnlineFinalRepository.save(finalLog);
            log.info("✅ AccountOnlineFinal updated AML info for Legal ID: {}", amlStatus.getLegalId());
            return finalLog;

        } catch (Exception e) {
            log.error("❌ Failed to update AccountOnlineFinal for Legal ID {}: {}", amlStatus.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update AML info in AccountOnlineFinal", e);
        }
    }

    // === Helper methods ===
    private LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) return null;
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            log.warn("⚠️ Could not parse date: {}", dateStr);
            return null;
        }
    }

    private ClsProvinceDto safeProvinceLookup(String code) {
        try { return code != null ? masterDataService.getProvinceByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ Province lookup failed for code {}", code); return null; }
    }

    private ClsDistrictDto safeDistrictLookup(String code) {
        try { return code != null ? masterDataService.getDistrictByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ District lookup failed for code {}", code); return null; }
    }

    private ClsCommuneDto safeCommuneLookup(String code) {
        try { return code != null ? masterDataService.getCommuneByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ Commune lookup failed for code {}", code); return null; }
    }

    private ClsVillageDto safeVillageLookup(String code) {
        try { return code != null ? masterDataService.getVillageByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ Village lookup failed for code {}", code); return null; }
    }

    private ClsBranchDto safeBranchLookup(String code) {
        try { return code != null ? masterDataService.getBranchByCode(code) : null; }
        catch (Exception e) { log.warn("⚠️ Branch lookup failed for code {}", code); return null; }
    }
    @Override
    public AccountOnlineFinalResponseDto findAccountByCifOrLegalId(AccountOnlineFinalLogRequestDto requestDto) {

        AccountOnlineFinal onlineFinal = accountOnlineFinalRepository
                .findTopByCifOrLegalIdOrderByCreatedAtDesc(requestDto.getCif(), requestDto.getLegalId())
                .orElseThrow(() -> new NotFoundException(
                        "Account not found for CIF: " + requestDto.getCif() + " or Legal ID: " + requestDto.getLegalId()
                ));

        log.info("Save data CIF : {} and Legal Id : {} to history ",requestDto.getCif(),requestDto.getLegalId());
        AccountOnlineOpenFinalAudit audit = new AccountOnlineOpenFinalAudit();
        // set data
        audit.setCif(requestDto.getCif());
        audit.setLegalId(requestDto.getLegalId());
        audit.setUser(securityUtils.getCurrentUser());
        audit.setAccount(onlineFinal);

        onlineFinalAuditRepository.save(audit);
        return mapper.toDto(onlineFinal);
    }


}
