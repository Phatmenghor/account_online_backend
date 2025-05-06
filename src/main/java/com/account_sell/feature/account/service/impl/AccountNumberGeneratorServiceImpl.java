package com.account_sell.feature.account.service.impl;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.FilterType;
import com.account_sell.exceptions.error.InvalidInputException;
import com.account_sell.feature.account.dto.request.GenerateAccountRequest;
import com.account_sell.feature.account.dto.resposne.GenerateAccountResponse;
import com.account_sell.feature.account.mapper.SpecialAccountMapper;
import com.account_sell.feature.account.service.AccountNumberGeneratorService;
import com.account_sell.utils.AccountNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountNumberGeneratorServiceImpl implements AccountNumberGeneratorService {

    private final SpecialAccountMapper specialAccountMapper;

    @Override
    public GenerateAccountResponse generateSpecialAccountNumbers(GenerateAccountRequest request, int limit) {
        log.info("Generating special account numbers with pattern: {}, price range: {} - {}, filter: {}, limit: {}, type: {}",
                request.getInputNumber(), request.getMinPrice(), request.getMaxPrice(),
                request.getFilter(), limit, request.getAccountType());

        // Validate request
        validateRequest(request);

        // Apply default limit if not specified or larger than max
        if (limit <= 0 || limit > 100000) {
            limit = 10;
            log.debug("Applying default limit of 10");
        }

        String inputPattern = request.getInputNumber();
        double minPrice = request.getMinPrice();
        double maxPrice = request.getMaxPrice();

        FilterType filterType = request.getFilter();
        if (filterType == null) {
            filterType = FilterType.CONTAIN;
            log.debug("No filter specified, defaulting to CONTAIN");
        }

        AccountType accountType = request.getAccountType();
        if (accountType == null) {
            accountType = AccountType.NORMAL;
            log.debug("No account type specified, defaulting to NORMAL");
        }

        // Generate account numbers
        List<Map.Entry<String, Double>> generatedAccounts = AccountNumberUtil.generateAccountNumbers(
                inputPattern, filterType.name(), limit, minPrice, maxPrice, accountType);

        log.info("Generated {} account numbers matching criteria", generatedAccounts.size());

        // Convert to response format using mapper
        List<GenerateAccountResponse.AccountDetails> accountDetails =
                specialAccountMapper.toAccountDetailsList(generatedAccounts);

        // Build the final response object
        return GenerateAccountResponse.builder()
                .accountNumberDetails(accountDetails)
                .totalCount(accountDetails.size())
                .generatedCount(accountDetails.size())
                .build();
    }

    /**
     * Validates the request parameters
     *
     * @param request the request to validate
     * @throws InvalidInputException if request is invalid
     */
    private void validateRequest(GenerateAccountRequest request) {
        if (request.getInputNumber() == null || request.getInputNumber().trim().isEmpty()) {
            log.error("Input number pattern is required");
            throw new InvalidInputException("Input number pattern is required");
        }

        String input = request.getInputNumber().trim();

        if (input.length() > 9) {
            log.error("Input must not exceed 9 digits: {}", input);
            throw new InvalidInputException("Input must not exceed 9 digits");
        }

        if (!input.matches("\\d+")) {
            log.error("Input must contain only digits: {}", input);
            throw new InvalidInputException("Input must contain only digits");
        }

        if (request.getMinPrice() < 0) {
            log.error("Minimum price cannot be negative: {}", request.getMinPrice());
            throw new InvalidInputException("Minimum price cannot be negative");
        }

        if (request.getMaxPrice() < request.getMinPrice()) {
            log.error("Maximum price must be greater than or equal to minimum price: {} > {}",
                    request.getMinPrice(), request.getMaxPrice());
            throw new InvalidInputException("Maximum price must be greater than or equal to minimum price");
        }
    }
}