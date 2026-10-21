
package com.hostelhub.payments.service;

import com.hostelhub.payments.entity.Payment;
import com.hostelhub.payments.repository.PaymentRepository;
import com.hostelhub.hostler.service.HostelAccessService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentProofService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final PaymentRepository paymentRepository;
    private final HostelAccessService accessService;
    private final S3Client s3Client;
    private final String bucket;

    public PaymentProofService(
            PaymentRepository paymentRepository,
            HostelAccessService accessService,
            S3Client s3Client,
            @Value("${payment.proof-storage.bucket}") String bucket
    ) {
        this.paymentRepository = paymentRepository;
        this.accessService = accessService;
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Transactional
    public Payment upload(
            Long hostlerId,
            Long paymentId,
            MultipartFile file
    ) {
        Payment payment = paymentRepository.findByIdAndHostlerId(
                paymentId, hostlerId
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "Payment not found for this hostler."
                )
        );

        if (payment.getMethod() != Payment.Method.MANUAL
                || payment.getStatus() != Payment.Status.SUBMITTED) {
            throw new IllegalArgumentException(
                    "Proof can only be uploaded for submitted manual payments."
            );
        }

        if (file == null || file.isEmpty()
                || file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Select a non-empty proof file up to 5 MB."
            );
        }

        try {
            byte[] bytes = file.getBytes();
            String contentType = detectContentType(bytes);

            if (contentType == null) {
                throw new IllegalArgumentException(
                        "Only JPEG, PNG or PDF proof files are accepted."
                );
            }

            String extension = switch (contentType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "application/pdf" -> ".pdf";
                default -> throw new IllegalArgumentException(
                        "Unsupported file type."
                );
            };

            String key = "hostels/" + payment.getHostelId()
                    + "/payments/" + paymentId + "/"
                    + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );

            payment.setProofFileKey(key);
            payment.setProofOriginalFilename(
                    safeFilename(file.getOriginalFilename())
            );
            payment.setProofContentType(contentType);
            payment.setProofUploadedAt(OffsetDateTime.now());

            return paymentRepository.saveAndFlush(payment);

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to read payment proof.", ex
            );
        } catch (software.amazon.awssdk.services.s3.model.S3Exception ex) {
            throw new IllegalStateException(
                    "Unable to upload payment proof to S3. Check AWS credentials, "
                            + "bucket region and IAM permissions.", ex
            );
        }
    }

    @Transactional(readOnly = true)
    public ProofFile getForHostel(
            String authenticatedEmail,
            Long hostelId,
            Long paymentId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail, hostelId
        );

        Payment payment = paymentRepository.findByIdAndHostelId(
                paymentId, hostelId
        ).orElseThrow(() ->
                new IllegalArgumentException("Payment not found.")
        );

        String key = payment.getProofFileKey();

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "No payment proof has been uploaded for this payment."
            );
        }

        ResponseBytes<GetObjectResponse> result = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        String contentType = payment.getProofContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = result.response().contentType();
        }

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return new ProofFile(
                result.asByteArray(),
                contentType,
                payment.getProofOriginalFilename()
        );
    }

    public record ProofFile(
            byte[] bytes,
            String contentType,
            String filename
    ) {}

    private String detectContentType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }

        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4e
                && bytes[3] == 0x47
                && bytes[4] == 0x0d
                && bytes[5] == 0x0a
                && bytes[6] == 0x1a
                && bytes[7] == 0x0a) {
            return "image/png";
        }

        if (bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-') {
            return "application/pdf";
        }

        return null;
    }

    private String safeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "payment-proof";
        }

        String name = Paths.get(original)
                .getFileName()
                .toString();

        return name.length() > 255
                ? name.substring(name.length() - 255)
                : name;
    }
}
