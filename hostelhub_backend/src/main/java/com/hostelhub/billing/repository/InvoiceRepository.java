package com.hostelhub.billing.repository;

import com.hostelhub.billing.entity.Invoice;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByHostlerIdOrderByPeriodStartDesc(
            Long hostlerId
    );

    List<Invoice> findByHostelIdOrderByPeriodStartDesc(
            Long hostelId
    );

    List<Invoice> findByHostelIdAndStatusOrderByPeriodStartDesc(
            Long hostelId,
            Invoice.Status status
    );

    Optional<Invoice> findByIdAndHostelId(
            Long id,
            Long hostelId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM Invoice i
        WHERE i.id = :invoiceId
          AND i.hostelId = :hostelId
        """)
    Optional<Invoice> findByIdAndHostelIdForUpdate(
            @Param("invoiceId") Long invoiceId,
            @Param("hostelId") Long hostelId
    );

    boolean existsByAllocationIdAndPeriodStartAndPeriodEnd(
            Long allocationId,
            LocalDate start,
            LocalDate end
    );


}
