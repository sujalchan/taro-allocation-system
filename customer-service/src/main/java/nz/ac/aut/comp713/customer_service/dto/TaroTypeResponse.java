package nz.ac.aut.comp713.customer_service.dto;

import java.math.BigDecimal;

public record TaroTypeResponse(
                Long id,
                String name,
                String description,
                BigDecimal standardPrice) {
}