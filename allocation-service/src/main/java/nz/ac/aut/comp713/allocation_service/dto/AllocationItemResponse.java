package nz.ac.aut.comp713.allocation_service.dto;

import java.math.BigDecimal;

public record AllocationItemResponse(

        Long id,
        Long taroTypeId,
        String taroTypeName,
        Integer quantity,
        BigDecimal pricePerKg) {
}
