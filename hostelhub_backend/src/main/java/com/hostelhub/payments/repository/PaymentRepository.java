package com.hostelhub.payments.repository;

import com.hostelhub.payments.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByHostlerIdOrderByCreatedAtDesc(Long hostlerId);

    List<Payment> findByHostelIdOrderByCreatedAtDesc(Long hostelId);

    List<Payment> findByHostelIdAndStatusOrderByCreatedAtDesc(
            Long hostelId, Payment.Status status
    );

    Optional<Payment> findByIdAndHostelId(Long id, Long hostelId);

    Optional<Payment> findByIdAndHostlerId(Long id, Long hostlerId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.gatewayOrderId = :orderId")
    Optional<Payment> findByGatewayOrderIdForUpdate(
            @Param("orderId") String orderId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id and p.hostelId = :hostelId")
    Optional<Payment> findByIdAndHostelIdForUpdate(
            @Param("id") Long id,
            @Param("hostelId") Long hostelId
    );
}