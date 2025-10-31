package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineSuccessLog;
import com.internal.feature.logs_report.repository.AccountOnlineSuccessLogRepository;
import com.internal.feature.logs_report.service.AccountOnlineOpenSuccessService;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOnlineOpenSuccessServiceImpl implements AccountOnlineOpenSuccessService {

    private final AccountOnlineSuccessLogRepository accountOnlineSuccessLogRepository;

    @Override
    public AccountOnlineSuccessLog saveSuccessLog(CustomerRequest request, CustomerImageUploadResponseDto imagePaths) {
        try {
            AccountOnlineSuccessLog successLog = AccountOnlineSuccessLog.builder()
                    .recId(request.getRecId())
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
                    .nationality(request.getNationality())
                    .releasedBy(request.getReleasedBy())
                    .averageIncome(request.getAverageIncome())
                    .legalDocName(request.getLegalDocName())
                    .occupation(request.getOccupation())
                    .customerProvince(request.getCustomerProvince())
                    .customerDistrict(request.getCustomerDistrict())
                    .customerCommune(request.getCustomerCommune())
                    .customerVillage(request.getCustomerVillage())
                    .phoneNumber(request.getPhoneNumber())
                    .nidImage(imagePaths.getNidImagePath())
                    .selfieImage(imagePaths.getSelfieImagePath())
                    .build();

            accountOnlineSuccessLogRepository.save(successLog);
            log.info("✅ AccountOnlineSuccessLog saved successfully for Legal ID: {}", request.getLegalId());
            return successLog;

        } catch (Exception e) {
            log.error("❌ Failed to save AccountOnlineSuccessLog for Legal ID {}: {}", request.getLegalId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save AccountOnlineSuccessLog", e);
        }
    }
}
