package nz.ac.aut.comp713.allocation_service.exception;

public class WeeklyAllocationNotFoundException extends RuntimeException {

    public WeeklyAllocationNotFoundException(Long allocationId) {
        super("Weekly allocation not found with id: " + allocationId);
    }
}