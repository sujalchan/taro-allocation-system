package nz.ac.aut.comp713.allocation_service.client;

import java.math.BigDecimal;

public record TaroTypeResponse(
        Long id,
        String name,
        String description,
        BigDecimal standardPrice) {
}