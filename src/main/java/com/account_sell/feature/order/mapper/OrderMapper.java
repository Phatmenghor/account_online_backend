package com.account_sell.feature.order.mapper;

import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.models.OrderEntity;
import com.account_sell.feature.order.models.OrderHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "ratePrice", target = "ratePrice")
    OrderResponse toOrderResponse(OrderEntity order);

    List<OrderResponse> toOrderResponseList(List<OrderEntity> orders);

    @Mapping(source = "order", target = "order")
    @Mapping(source = "order.accountNumber", target = "accountNumber")
    @Mapping(source = "order.customerName", target = "customerName")
    OrderHistoryResponse toOrderHistoryResponse(OrderHistoryEntity history);

    List<OrderHistoryResponse> toOrderHistoryResponseList(List<OrderHistoryEntity> historyList);

    default <T> OrderListResponse<T> toListResponse(Page<?> page, List<T> content) {
        return OrderListResponse.<T>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
