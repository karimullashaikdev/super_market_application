package com.karim.dto;

/**
 * Broadcast over WebSocket whenever an order status changes.
 * Sent to:
 *   /topic/orders          → delivery dashboard (all agents)
 *   /topic/order/{orderId} → customer tracking page (specific order)
 */
public class OrderStatusEvent {

    private Long orderId;
    private String status;       // PAID | ASSIGNED | OUT_FOR_DELIVERY | DELIVERED
    private String agentName;    // null until assigned
    private String agentMobile;  // null until assigned
    private String agentId;      // null until assigned

    public OrderStatusEvent() {}

    public OrderStatusEvent(Long orderId, String status, String agentId, String agentName, String agentMobile) {
        this.orderId = orderId;
        this.status = status;
        this.agentId = agentId;
        this.agentName = agentName;
        this.agentMobile = agentMobile;
    }

    // ── Getters & Setters ──

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getAgentMobile() { return agentMobile; }
    public void setAgentMobile(String agentMobile) { this.agentMobile = agentMobile; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}