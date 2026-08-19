package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.customer.CustomerRequest;
import io.booking.booking_service.dto.pojo.customer.CustomerResponse;
import io.booking.booking_service.dto.pojo.customer.CustomerUpdate;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerService {
    Mono<CustomerResponse> create(String requestId, CustomerRequest request);
    Mono<CustomerResponse> update(String requestId, UUID id, CustomerUpdate request);
    Mono<CustomerResponse> get(String requestId, UUID id);
    Mono<CustomerResponse> getByCustomerNumber(String requestId, String customerNumber);
    Mono<CustomerResponse> getByEmail(String requestId, String email);
    Mono<Void> deactivate(String requestId, UUID id);
}
