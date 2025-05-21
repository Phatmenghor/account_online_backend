package com.account_sell.feature.order.mapper;

import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.models.OrderEntity;
import com.account_sell.feature.order.models.OrderHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "ratePrice", target = "ratePrice")
    OrderResponse toOrderResponse(OrderEntity order);

    List<OrderResponse> toOrderResponseList(List<OrderEntity> orders);

    @Mapping(source = "order", target = "order")
    @Mapping(source = "order.accountNumber", target = "accountNumber")
    @Mapping(source = "order.customerName", target = "customerName")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.username", target = "user.email")
    @Mapping(source = "user.roles", target = "user.userRole", qualifiedByName = "mapRoles")
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

    /**
     * Maps a list of roles to a comma-separated string of role names.
     * @param roles list of roles
     * @return comma-separated string of role names
     */
    @Named("mapRoles")
    default String mapRoles(List<com.account_sell.feature.auth.models.Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", "));
    }
}
