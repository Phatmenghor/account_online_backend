package com.account_sell.feature.order.service;

import com.account_sell.feature.order.dto.request.*;
import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.dto.response.ValidateAccountNumberResponse;

import java.util.List;

public interface OrderService {
    // Account validation
    ValidateAccountNumberResponse validateAccountNumber(ValidateAccountNumberRequest request);

    ValidateAccountNumberResponse validateAccountBank(ValidateAccountNumberRequest request);

    // Order CRUD operations
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderHistoryResponse getOrderHistoryById(Long id);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    // Order listings with pagination and search
    OrderListResponse<OrderResponse> getBookedOrders(OrderFilterRequest request);

    OrderListResponse<OrderHistoryResponse> getOrderHistory(OrderFilterRequest request);

    // Order history don't have pagination
    List<OrderHistoryResponse> getOrderHistoryNoPage(OrderFilterNoPageRequest request);

    // Scheduled cleanup task
    void processOldOrders();
}