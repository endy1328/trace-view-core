package com.example.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "billingClient", url = "https://billing.example.com")
public interface BillingClient {

    @GetMapping("/billing/{orderId}")
    String getBillingStatus(@PathVariable("orderId") Long orderId);
}
