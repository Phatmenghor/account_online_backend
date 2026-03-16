package com.internal.feature.open_account.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.master_data.dto.response.OccupationDto;
import com.internal.feature.master_data.service.OccupationService;
import com.internal.feature.open_account.dto.request.CustomerAmlRequest;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.AmlExternalResponseDto;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.mapper.OpenAccountAmlStatusMapper;
import com.internal.feature.open_account.service.external.AmlMiddlewareService;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
// Service to handle compliance checks
public class ComplianceService {

    private final AmlService amlService;
    private final AmlMiddlewareService amlMiddlewareService;
    private final OccupationService occupationService;
    private final OpenAccountAmlStatusMapper openAccountAmlStatusMapper;
    private final ObjectMapper objectMapper;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;

    public AmlStatusDto processAml(CustomerRequest request) throws Exception {
        log.info("========== START AML Processing for Legal ID: {} ==========", request.getLegalId());

        // Always check existing AML status first before calling middleware
        Optional<AmlStatus> existingAmlOpt = amlService.findByLegalId(request.getLegalId());
        if (existingAmlOpt.isPresent()) {
            log.info("Existing AML status found for Legal ID: {} | Status: {}", request.getLegalId(), existingAmlOpt.get().getStatus());
            return handleExistingAml(existingAmlOpt.get(), request.getLegalId());
        }

        // Build request and call AML middleware
        CustomerAmlRequest amlRequestDto = openAccountAmlStatusMapper.buildAmlRequestDto(request);
        AmlExternalResponseDto amlResponse = callAmlMiddleware(amlRequestDto, request.getLegalId());

        // Build occupation status string
        String occupationStatus = buildOccupationStatus(request.getOccupation());

        // Determine AML status based on risk
        boolean isHighRisk = AppConstants.RISK_HIGH.equalsIgnoreCase(amlResponse.getRiskLevel());
        AmlStatusEnum amlStatusEnum = isHighRisk ? AmlStatusEnum.PENDING : AmlStatusEnum.APPROVE;

        // Map to CreateAmlRequestDto
        CreateAmlRequestDto createRequest = openAccountAmlStatusMapper.toCreateRequest(amlRequestDto, amlResponse,
                request, occupationStatus, amlStatusEnum, objectMapper);

        // Handle high-risk customers
        if (isHighRisk) {
            log.warn("HIGH RISK customer detected | Legal ID: {} | RiskLevel: {} | Rules: {}",
                    request.getLegalId(), amlResponse.getRiskLevel(), amlResponse.getRulesTriggered());
            amlService.createAmlStatus(createRequest);
            sendAmlNotification(amlRequestDto, amlResponse, request);
        } else {
            log.info("LOW RISK customer approved | Legal ID: {} | RiskLevel: {}",
                    request.getLegalId(), amlResponse.getRiskLevel());
        }

        log.info("========== END AML Processing for Legal ID: {} | Status: {} ==========", request.getLegalId(), amlStatusEnum);

        // Low-risk → return mapped DTO
        return openAccountAmlStatusMapper.fromRequestAndResponse(request, amlResponse, amlStatusEnum);
    }

    public void sentMessageOnHighRisk(CustomerRequest request, AmlStatusDto amlProcessResult) {
        if (amlProcessResult.getStatus() == AmlStatusEnum.PENDING) {
            throw new AccountCreationException(String.format(AppConstants.AML_NEED_REVIEW_MSG, request.getLegalId()));
        } else if (amlProcessResult.getStatus() == AmlStatusEnum.REJECT) {
            throw new AccountCreationException(String.format(AppConstants.AML_REJECTED_MSG, request.getLegalId()));
        }
    }

    public CustomerResponse buildCustomerAccInfo(String cif, String khrAccount, String usdAccount, String mnemonic) {
        return openAccountAmlStatusMapper.buildCustomerAccInfo(cif, khrAccount, usdAccount, mnemonic);
    }

    private AmlStatusDto handleExistingAml(AmlStatus existing, String legalId) {
        return switch (existing.getStatus()) {
            case APPROVE -> openAccountAmlStatusMapper.toDto(existing);
            case PENDING ->
                throw new AccountCreationException(String.format(AppConstants.AML_NEED_REVIEW_MSG, legalId));
            case REJECT -> throw new AccountCreationException(String.format(AppConstants.AML_REJECTED_MSG, legalId));
            default -> throw new AccountCreationException(String.format(AppConstants.AML_UNKNOWN_MSG, legalId));
        };
    }

    private AmlExternalResponseDto callAmlMiddleware(CustomerAmlRequest amlRequest, String legalId)
            throws JsonProcessingException {
        AmlExternalResponseDto response = amlMiddlewareService.CheckAml(amlRequest);
        log.info("AML Middleware response received | RiskLevel: {} | TrxnID: {}", response.getRiskLevel(),
                response.getTrxnID());
        return response;
    }

    private String buildOccupationStatus(String occupationCode) {
        OccupationDto occupation = safeOccupationLookup(occupationCode);
        return occupation != null ? occupation.getNameEn() + " / " + occupation.getNameKh() : "";
    }

    private OccupationDto safeOccupationLookup(String code) {
        if (code == null) {
            return null;
        }
        return occupationService.getOccupationByCode(code)
                .orElseGet(() -> {
                    log.warn("Occupation lookup failed for code: {}", code);
                    return null;
                });
    }

    private void sendAmlNotification(CustomerAmlRequest amlRequest, AmlExternalResponseDto amlResponse,
            CustomerRequest request) {
        log.info(">>> ENTER sendAmlNotification() for Legal ID: {}", request.getLegalId());
        try {
            AmlStatusDto amlDto = openAccountAmlStatusMapper.fromRequestAndResponse(request, amlResponse,
                    AmlStatusEnum.PENDING);

            try {
                alertTelegramService.sendTelegramAmlProcess(amlDto);
                log.info("Telegram AML notification sent successfully.");
            } catch (Exception e) {
                log.error("Telegram notification failed: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Notification logic failed for Legal ID {}: {}", request.getLegalId(), e.getMessage());
        }
    }
}
