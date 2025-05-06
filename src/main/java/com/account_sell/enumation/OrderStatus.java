package com.account_sell.enumation;

public enum OrderStatus {
    BOOKED,    // Initial status when order is created
    ACCEPTED,  // Order is accepted by admin
    REJECTED,  // Order is rejected by admin
    EXPIRED,   // Order expired after 2 weeks without update
    COMPLETED, // Order processing is completed
    CANCELLED,  // Order is cancelled by customer
    ALL
}