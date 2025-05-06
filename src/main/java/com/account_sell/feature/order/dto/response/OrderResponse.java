package com.account_sell.feature.order.dto.response;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.FilterType;
import com.account_sell.enumation.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal price;
    private String ratePrice;
    private String customerName;
    private String phoneNumber;
    private String idNumber;
    private AccountType accountType;
    private FilterType filterType;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}