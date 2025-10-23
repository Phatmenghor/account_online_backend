package com.internal.feature.open_account.service.impl;

import com.internal.exceptions.error.openaccount.AccountCreationException;
import com.internal.exceptions.error.openaccount.CustomerCreationException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.feature.open_account.dto.response.CustomerResponse;
import com.internal.feature.open_account.service.*;
import com.internal.feature.open_account.service.external.MobileBankingService;
import com.internal.feature.open_account.service.external.T24Service;
import com.internal.feature.open_account.service.external.ValidationService;
import com.internal.feature.open_account.service.external.XmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountServiceImpl implements OpenAccountService {

    private final ValidationService validationService;
    private final T24Service t24Service;
    private final MobileBankingService mobileBankingService;

    @Override
    @Transactional
    public CustomerResponse openAccount(CustomerRequest request) {
        log.info("Processing account opening for Legal ID: {}", request.getLegalId());

        try {
            // Step 1: Check database connections
            validationService.checkDatabaseConnections();

            // Step 2: Get customer info
            Map<String, String> customerInfo = validationService.getCustomerInfo(request.getLegalId());

            log.info("Testing ,{}", customerInfo);
//
//            // Step 3: Validate customer rating
//            validationService.validateCustomerRating(customerInfo);
//
//            // Step 4: Validate existing accounts
//            validationService.validateExistingAccounts(customerInfo);
//
//            // Step 5: Create customer if needed
//            String cif = customerInfo.get("CIF");
//            if (cif == null || cif.isEmpty()) {
//                cif = createCustomer(request);
//            } else {
//                log.info("Using existing CIF: {}", cif);
//            }
//
//            // Step 6: Create accounts
//            String khrAccount = null;
//            String usdAccount = null;
//
//            if (!validationService.hasAccount(customerInfo, "KHR")) {
//                khrAccount = createAccount(request, cif, "KHR");
//            }
//
//            if (!validationService.hasAccount(customerInfo, "USD")) {
//                usdAccount = createAccount(request, cif, "USD");
//            }
//
//            // Step 7: Validate at least one account created
//            if (khrAccount == null && usdAccount == null &&
//                !validationService.hasAccount(customerInfo, "KHR") &&
//                !validationService.hasAccount(customerInfo, "USD")) {
//                throw new AccountCreationException("Failed to create any accounts");
//            }
//
//            // Step 8: Activate mobile banking (non-blocking)
//            try {
//                mobileBankingService.activate(request, cif, khrAccount, usdAccount);
//            } catch (Exception e) {
//                log.error("Mobile banking activation failed (non-critical): {}", e.getMessage());
//            }
//            // Step 9: Return success response
//            return CustomerResponse.builder()
//                .errCode("200")
//                .errMsg("Account Create Success")
//                .status("NEW")
//                .content("ការស្នើសុំជោគជ័យ។ លោកអ្នកនឹងទទួលបានលេខគណនី តាមសារទូរស័ព្ទ។")
//                .cif(cif)
//                .khrAccount(khrAccount)
//                .usdAccount(usdAccount)
//                .build();


            // Step 9: Return success response
            return CustomerResponse.builder()
                    .errCode("200")
                    .errMsg("Account Create Success")
                    .status("NEW")
                    .content("ការស្នើសុំជោគជ័យ។ លោកអ្នកនឹងទទួលបានលេខគណនី តាមសារទូរស័ព្ទ។")
                    .cif(null)
                    .khrAccount(null)
                    .usdAccount(null)
                    .build();

        } catch (Exception e) {
            log.error("Account opening failed: {}", e.getMessage());
            throw e;
        }
    }

    private String createCustomer(CustomerRequest request) {
        log.info("Creating new customer for Legal ID: {}", request.getLegalId());

        Document response = t24Service.createCustomer(request);
        if (response == null) {
            throw new CustomerCreationException("T24 returned null response");
        }

        String cif = XmlParser.extractCif(response);
        if (cif == null || cif.isEmpty()) {
            throw new CustomerCreationException("No CIF returned from T24");
        }

        log.info("Customer created with CIF: {}", cif);
        return cif;
    }

    private String createAccount(CustomerRequest request, String cif, String currency) {
        try {
            log.info("Creating {} account for CIF: {}", currency, cif);

            Document response = t24Service.createAccount(request, cif, currency);
            if (response == null) {
                log.warn("Failed to create {} account - null response", currency);
                return null;
            }

            String accountNumber = XmlParser.extractAccountNumber(response);
            log.info("{} account created: {}", currency, accountNumber);
            return accountNumber;

        } catch (Exception e) {
            log.error("Error creating {} account: {}", currency, e.getMessage());
            return null;
        }
    }
}