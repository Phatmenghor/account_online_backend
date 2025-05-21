package com.account_sell.feature.order.dto.request;

import com.account_sell.enumation.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterNoPageRequest {
    private OrderStatus status;
    private String startDate;
    private String endDate;
    private String search;
    private Long userId;
}
