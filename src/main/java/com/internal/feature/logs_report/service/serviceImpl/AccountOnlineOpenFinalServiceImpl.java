package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.logs_report.dto.request.AccountOnlineFinalLogRequestDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.mapper.AccountOnlineFinalMapper;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import com.internal.feature.logs_report.model.AccountOnlineOpenFinalAudit;
import com.internal.feature.logs_report.model.AccountOnlineSuccessLog;
import com.internal.feature.logs_report.repository.AccountOnlineFinalAuditRepository;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOnlineOpenFinalServiceImpl implements AccountOnlineOpenFinalService {

    private final AccountOnlineFinalRepository accountOnlineFinalRepository;
    private final AccountOnlineFinalAuditRepository onlineFinalAuditRepository;
    private final AccountOnlineFinalMapper mapper;
    private final SecurityUtils securityUtils;

    @Override
    public AccountOnlineSuccessLog saveFinalLog(CustomerRequest request, CustomerImageUploadResponseDto imagePaths) {
        try {
            AccountOnlineSuccessLog successLog = AccountOnlineSuccessLog.builder()
                    .legalId(request.getLegalId())
                    .familyName(request.getFamilyName())
                    .givenName(request.getGivenName())
                    .firstNameKh(request.getFirstNameKh())
                    .lastNameKh(request.getLastNameKh())
                    .dateOfBirth(request.getDateOfBirth())
                    .legalAddress(request.getLegalAddress())
                    .gender(request.getGender())
                    .maritalStatus(request.getMaritalStatus())
                    .companyName(request.getCompanyName())
                    .referralId(request.getReferralId())
                    .branchCode(request.getBranchCode())
                    .placeOfBirth(request.getPlaceOfBirth())
                    .nationality("KH")
                    .releasedBy("")
                    .averageIncome("0")
                    .legalDocName("NATIONAL.ID")
                    .occupation(request.getOccupation())
                    .customerProvince(request.getCustomerCurrentProvince())
                    .customerDistrict(request.getCustomerCurrentDistrict())
                    .customerCommune(request.getCustomerCurrentCommune())
                    .customerVillage(request.getCustomerCurrentVillage())
                    .phoneNumber(request.getPhoneNumber())
                    .nidImage(imagePaths != null ? imagePaths.getNidImagePath() : null)
                    .selfieImage(imagePaths != null ? imagePaths.getSelfieImagePath() : null)
                    .build();

            accountOnlineFinalRepository.save(null);
            log.info("✅ AccountOnlineSuccessLog saved successfully for Legal ID: {}", request.getLegalId());
            return successLog;

        } catch (Exception e) {
            log.error("❌ Failed to save AccountOnlineSuccessLog for Legal ID {}: {}", request.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save AccountOnlineSuccessLog", e);
        }
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
