package com.hostelhub.payments.controller;

import com.hostelhub.payments.dto.*;
import com.hostelhub.payments.service.PaymentService;
import com.hostelhub.payments.service.RazorpayPaymentService;
import com.hostelhub.security.AuthenticatedHostlerId;
import com.hostelhub.security.AuthenticatedUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayPaymentService razorpayService;

    public PaymentController(
            PaymentService paymentService,
            RazorpayPaymentService razorpayService
    ) {
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    @GetMapping("/mine")
    public List<PaymentResponse> mine(Authentication auth) {
        return paymentService.listMine(AuthenticatedHostlerId.from(auth));
    }

    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse submitManual(
            Authentication auth,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return paymentService.submitManualPayment(
                AuthenticatedHostlerId.from(auth), request
        );
    }

    @PostMapping("/razorpay/order")
    @ResponseStatus(HttpStatus.CREATED)
    public RazorpayOrderResponse createOrder(
            Authentication auth,
            @Valid @RequestBody CreateRazorpayOrderRequest request
    ) {
        return razorpayService.createOrder(
                AuthenticatedHostlerId.from(auth), request
        );
    }

    @PostMapping("/razorpay/verify")
    public RazorpayVerificationResponse verify(
            Authentication auth,
            @Valid @RequestBody VerifyRazorpayPaymentRequest request
    ) {
        return razorpayService.verifyCheckout(
                AuthenticatedHostlerId.from(auth), request
        );
    }

    @GetMapping("/hostel/{hostelId}")
    public List<PaymentResponse> hostelPayments(
            Authentication auth,
            @PathVariable Long hostelId,
            @RequestParam(required = false) String status
    ) {
        return paymentService.listForHostel(
                auth.getName(), hostelId, status
        );
    }

    @PostMapping("/hostel/{hostelId}/{paymentId}/review")
    public PaymentResponse review(
            Authentication auth,
            @PathVariable Long hostelId,
            @PathVariable Long paymentId,
            @Valid @RequestBody ReviewPaymentRequest request
    ) {
        return paymentService.reviewManualPayment(
                auth.getName(),
                hostelId,
                paymentId,
                AuthenticatedUserId.from(auth),
                request.approve(),
                request.note()
        );
    }
}