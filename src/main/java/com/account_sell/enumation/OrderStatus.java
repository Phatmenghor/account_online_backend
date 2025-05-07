package com.account_sell.enumation;

public enum OrderStatus {
    BOOKED,    // Initial status when order is created
    ACCEPTED,  // Order is accepted by admin
    COMPLETED,
    REJECTED,  // Order is rejected by admin
    EXPIRED   // Order expired after 2 weeks without update  // Order is cancelled by customer
}