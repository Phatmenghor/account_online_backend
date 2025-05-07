package com.account_sell.feature.order.service;

import com.account_sell.feature.order.dto.request.CreateOrderRequest;
import com.account_sell.feature.order.dto.request.OrderFilterRequest;
import com.account_sell.feature.order.dto.request.UpdateOrderStatusRequest;
import com.account_sell.feature.order.dto.request.ValidateAccountNumberRequest;
import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.dto.response.ValidateAccountNumberResponse;

public interface OrderService {
    // Account validation
    ValidateAccountNumberResponse validateAccountNumber(ValidateAccountNumberRequest request);

    ValidateAccountNumberResponse validateAccountBank(ValidateAccountNumberRequest request);

    // Order CRUD operations
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    // Order listings with pagination and search
    OrderListResponse<OrderResponse> getBookedOrders(OrderFilterRequest request);

    OrderListResponse<OrderHistoryResponse> getOrderHistory(OrderFilterRequest request);

    // Scheduled cleanup task
    void processOldOrders();
}