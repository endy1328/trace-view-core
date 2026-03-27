package com.example.orders.service;

import com.example.orders.client.BillingClient;
import com.example.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BillingClient billingClient;

    public OrderService(OrderRepository orderRepository, BillingClient billingClient) {
        this.orderRepository = orderRepository;
        this.billingClient = billingClient;
    }

    public String findOrder(Long id) {
        orderRepository.findById(id);
        billingClient.getBillingStatus(id);
        return "ok";
    }
}
