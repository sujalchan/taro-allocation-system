package nz.ac.aut.comp713.allocation_service.client;

public record CustomerResponse(
        Long id,
        String name,
        String contactName,
        String phone,
        Boolean active) {
}
