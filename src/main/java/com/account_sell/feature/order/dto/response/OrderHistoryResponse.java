package com.account_sell.feature.order.dto.response;

import com.account_sell.enumation.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private OrderResponse order;
    private String accountNumber;
    private String customerName;
    private OrderStatus newStatus;
    private String remarks;
    private LocalDateTime createdAt;
}