package nz.ac.aut.comp713.allocation_service.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyAllocationResponse(

        Long id,
        Long customerId,
        String customerName,
        LocalDate weekStart,
        List<AllocationItemResponse> allocationItems) {
}