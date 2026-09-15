package nz.ac.aut.comp713.allocation_service.exception;

import java.time.LocalDate;

public class WeeklyAllocationAlreadyExistsException extends RuntimeException {

    public WeeklyAllocationAlreadyExistsException(
            Long customerId,
            LocalDate weekStart) {

        super(
                "Weekly allocation already exists for customer "
                        + customerId
                        + " for week starting "
                        + weekStart);
    }
}