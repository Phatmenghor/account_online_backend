package com.account_sell.feature.account.controller;

import com.account_sell.feature.account.dto.request.GenerateAccountRequest;
import com.account_sell.feature.account.dto.resposne.GenerateAccountResponse;
import com.account_sell.feature.account.service.AccountNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/user/special-accounts")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserAccountController {

    private final AccountNumberGeneratorService accountNumberGeneratorService;
    /**
     * Generates special account numbers based on the provided criteria.
     *
     * @param request the request containing filtering criteria
     * @param limit optional parameter to limit the number of results (default: 10, max: 100)
     * @return response with generated account numbers and statistics
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateAccountResponse> generateSpecialAccountNumbers(
            @RequestBody @Valid GenerateAccountRequest request,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100000) int limit) {

        log.info("Received request to generate special account numbers with pattern: {}, price range: {} - {}, filter: {}, limit: {}",
                request.getInputNumber(), request.getMinPrice(), request.getMaxPrice(),
                request.getFilter(), limit);

        GenerateAccountResponse response = accountNumberGeneratorService.generateSpecialAccountNumbers(request, limit);

        log.info("Generated {} account numbers", response.getGeneratedCount());

        return ResponseEntity.ok(response);
    }
}
