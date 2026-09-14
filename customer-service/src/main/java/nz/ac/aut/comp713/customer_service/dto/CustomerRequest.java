package nz.ac.aut.comp713.customer_service.dto;

import jakarta.validation.constraints.NotBlank;

// DTO for customer request
public record CustomerRequest(
        @NotBlank(message = "Customer name is required") String name,
        String contactName,
        String phone,
        Boolean active) {
}
