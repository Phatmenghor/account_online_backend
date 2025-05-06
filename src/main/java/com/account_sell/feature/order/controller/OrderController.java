package com.account_sell.feature.order.controller;

import com.account_sell.config.RequiresRole;
import com.account_sell.enumation.OrderStatus;
import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.order.dto.request.CreateOrderRequest;
import com.account_sell.feature.order.dto.request.OrderFilterRequest;
import com.account_sell.feature.order.dto.request.UpdateOrderStatusRequest;
import com.account_sell.feature.order.dto.request.ValidateAccountNumberRequest;
import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.dto.response.ValidateAccountNumberResponse;
import com.account_sell.feature.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/validate")
    public ApiResponse<ValidateAccountNumberResponse> validateAccountNumber(
            @RequestBody @Valid ValidateAccountNumberRequest request) {

        log.info("Received request to validate account number: {}", request.getAccountNumber());

        ValidateAccountNumberResponse response = orderService.validateAccountNumber(request);

        log.info("Validation completed: {} - {}", request.getAccountNumber(), response.getMessage());

        return new ApiResponse<>(
                response.isValid() && response.isAvailable() ? "success" : "warning",
                response.getMessage(),
                response
        );
    }

    @PostMapping()
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request) {

        log.info("Received request to create order for account number: {}", request.getAccountNumber());

        OrderResponse response = orderService.createOrder(request);

        log.info("Order created successfully with ID: {}", response.getId());

        return new ApiResponse<>(
                "success",
                "Order created successfully",
                response
        );
    }

    @PostMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("Received request to get order with ID: {}", id);

        OrderResponse response = orderService.getOrderById(id);

        log.info("Successfully retrieved order with ID: {}", id);

        return new ApiResponse<>(
                "success",
                "Order retrieved successfully",
                response
        );
    }

    @PostMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {

        log.info("Received request to update status for order ID: {} to {}", id, request.getNewStatus());

        OrderResponse response = orderService.updateOrderStatus(id, request);

        log.info("Order status updated successfully for ID: {}", id);

        return new ApiResponse<>(
                "success",
                "Order status updated successfully",
                response
        );
    }

    @PostMapping("/booked")
    public ApiResponse<OrderListResponse<OrderResponse>> getBookedOrders(
            @Valid
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search) {

        log.info("Received request to get BOOKED orders - page: {}, size: {}, search: '{}'",
                pageNo - 1, pageSize, search);

        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setPageNo(pageNo - 1);
        filterRequest.setPageSize(pageSize);
        filterRequest.setSearch(search);

        OrderListResponse<OrderResponse> response = orderService.getBookedOrders(filterRequest);

        log.info("Successfully retrieved {} BOOKED orders", response.getTotalElements());

        return new ApiResponse<>(
                "success",
                "BOOKED orders retrieved successfully",
                response
        );
    }

    @PostMapping("/history")
    public ApiResponse<OrderListResponse<OrderHistoryResponse>> getOrderHistory(
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "search", required = false) String search) {

        log.info("Received request to get order history - page: {}, size: {}, status: {}, search: '{}'", pageNo - 1, pageSize, status, search);

        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setPageNo(pageNo - 1);
        filterRequest.setPageSize(pageSize);
        filterRequest.setSearch(search);

        // Set status if provided
        if (status != null) {
            filterRequest.setStatus(status);
        }

        OrderListResponse<OrderHistoryResponse> response = orderService.getOrderHistory(filterRequest);

        log.info("Successfully retrieved {} order history records", response.getTotalElements());

        return new ApiResponse<>(
                "success",
                "Order history retrieved successfully",
                response
        );
    }

    /**
     * Manually trigger the processing of old orders
     * (Admin only endpoint for manual cleanup)
     */
    @PostMapping("/cleanup")
    @RequiresRole(value = {"ADMIN"})
    public ApiResponse<String> triggerOrderCleanup() {
        log.info("Received request to manually trigger order cleanup");

        orderService.processOldOrders();

        log.info("Order cleanup process completed successfully");

        return new ApiResponse<>(
                "success",
                "Order cleanup process completed successfully",
                "All orders not updated for 2 weeks have been moved to EXPIRED status"
        );
    }
}