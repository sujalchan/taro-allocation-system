package nz.ac.aut.comp713.customer_service.dto;

import jakarta.validation.constraints.NotBlank;

// DTO for customer request
public record CustomerRequest(
                // Validation annotation to ensure the name is not blank
                @NotBlank(message = "Customer name is required") String name,

                String contactName,
                String phone,
                Boolean active) {
}
