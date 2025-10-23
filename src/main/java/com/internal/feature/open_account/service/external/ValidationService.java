package com.internal.feature.open_account.service.external;

import com.internal.exceptions.error.openaccount.AccountExistsException;
import com.internal.exceptions.error.openaccount.DatabaseConnectionException;
import com.internal.exceptions.error.openaccount.HighRiskCustomerException;
import com.internal.feature.open_account.repository.CustomerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final CustomerInfoRepository customerInfoRepository;

    public void checkDatabaseConnections() {
        try {
            customerInfoRepository.testConnection();
            log.debug("Database connection check successful");
        } catch (DataAccessException e) {
            log.error("Database connection failed: {}", e.getMessage());
            throw new DatabaseConnectionException("Failed to connect to database");
        }
    }

    public Map<String, String> getCustomerInfo(String legalId) {
        return customerInfoRepository.findByLegalId(legalId);
    }

    public void validateCustomerRating(Map<String, String> customerInfo) {
        String rating = customerInfo.get("RATING");
        if (rating != null && ("3".equals(rating) || "4".equals(rating))) {
            log.warn("High-risk customer detected with rating: {}", rating);
            throw new HighRiskCustomerException(rating);
        }
    }

    public void validateExistingAccounts(Map<String, String> customerInfo) {
        String accounts = customerInfo.get("ACCT");
        if (accounts != null && !accounts.isEmpty()) {
            String[] accountArray = accounts.split("#");
            boolean hasKHR = false;
            boolean hasUSD = false;
            
            for (String account : accountArray) {
                if ("KHR".equals(account)) hasKHR = true;
                if ("USD".equals(account)) hasUSD = true;
            }
            
            if (hasKHR && hasUSD) {
                String cif = customerInfo.get("CIF");
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