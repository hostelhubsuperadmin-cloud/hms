package com.hostelhub.hostel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHostelRequest(
        @NotBlank @Size(max = 150)
        String name,

        @NotBlank @Size(max = 50)
        String code,

        @Size(max = 2000)
        String description,

        @NotBlank @Size(max = 255)
        String addressLine,

        @NotBlank @Size(max = 100)
        String city,

        @NotBlank @Size(max = 100)
        String state,

        @NotBlank @Size(max = 100)
        String country,

        @Size(max = 20)
        String postalCode,

        @Size(max = 20)
        String contactPhone,

        @Email @Size(max = 254)
        String contactEmail
) {}
