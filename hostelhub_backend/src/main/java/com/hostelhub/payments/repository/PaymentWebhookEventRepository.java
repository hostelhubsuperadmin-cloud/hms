package com.hostelhub.payments.repository;

import com.hostelhub.payments.entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookEventRepository
        extends JpaRepository<PaymentWebhookEvent, String> {
}
