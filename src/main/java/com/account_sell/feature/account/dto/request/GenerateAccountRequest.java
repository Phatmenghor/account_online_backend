package com.account_sell.feature.account.dto.request;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAccountRequest {

    @NotBlank(message = "Input number pattern is required")
    @Size(min = 1, max = 9, message = "Input must be between 1 and 9 digits")
    private String inputNumber;

    @Min(value = 0, message = "Minimum price cannot be negative")
    private double minPrice;

    @Min(value = 0, message = "Maximum price cannot be negative")
    private double maxPrice;

    private FilterType filter = FilterType.CONTAIN;

    private AccountType accountType = AccountType.NORMAL;
}