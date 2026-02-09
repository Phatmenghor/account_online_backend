package com.internal.feature.open_account.service.external;

import com.internal.exceptions.error.openaccount.AccountExistsException;
import com.internal.exceptions.error.openaccount.HighRiskCustomerException;
import com.internal.feature.open_account.repository.CustomerInfoRepository;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final CustomerInfoRepository customerInfoRepository;

    public Map<String, String> getCustomerInfo(String legalId) {
        return customerInfoRepository.findByLegalId(legalId);
    }

    public void validateCustomerRating(Map<String, String> customerInfo) {
        String rating = customerInfo.get("RATING");
        if (("3".equals(rating) || "4".equals(rating))) {
            log.warn("High-risk customer detected with rating: {}", rating);
            throw new HighRiskCustomerException(rating);
        }
        log.info("Customer rating {} is good", rating);
    }

    public void validateExistingAccounts(Map<String, String> customerInfo) {
        String accounts = customerInfo.get("ACCT");
        if (accounts != null && !accounts.isEmpty()) {
            String[] accountArray = accounts.split("#");
            boolean hasKHR = false;
            boolean hasUSD = false;

            for (String account : accountArray) {
                if (AppConstants.CURRENCY_KHR.equals(account)) hasKHR = true;
                if (AppConstants.CURRENCY_USD.equals(account)) hasUSD = true;
            }

            String cif = customerInfo.get("CIF");

            if (hasKHR && hasUSD && cif != null && !cif.isEmpty()) {
                log.warn("Customer already has both accounts. CIF: {}", cif);
                throw new AccountExistsException(cif);
            }
        }
    }

    public boolean hasAccount(Map<String, String> customerInfo, String currency) {
        String accounts = customerInfo.get("ACCT");
        if (accounts == null || accounts.isEmpty()) {
            return false;
        }

        String[] accountArray = accounts.split("#");
        for (String account : accountArray) {
            if (currency.equals(account)) {
                return true;
            }
        }
        return false;
    }
}
