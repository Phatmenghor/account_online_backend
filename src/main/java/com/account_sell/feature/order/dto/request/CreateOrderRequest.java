package com.account_sell.feature.order.dto.request;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Account number is required")
    @Size(min = 9, max = 9, message = "Account number must be exactly 9 digits")
    @Pattern(regexp = "\\d{9}", message = "Account number must contain only digits")
    private String accountNumber;

    private BigDecimal price;
    private String ratePrice;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String phoneNumber;

    private String idNumber;

    private AccountType accountType;

    private FilterType filterType;
}