package com.example.payment_service.model;

public class MomoPaymentRequest {
    private Long amount;
    private String orderInfo;

    // Getters & Setters

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}