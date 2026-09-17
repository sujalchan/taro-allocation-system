package nz.ac.aut.comp713.allocation_service.exception;

import java.time.LocalDate;

public class WeeklyAllocationAlreadyExistsException extends RuntimeException {

    public WeeklyAllocationAlreadyExistsException(
            Long customerId,
            String customerName,
            LocalDate weekStart) {

        super(
                "Weekly allocation already exists for "
                        + customerName
                        + " for week starting "
                        + weekStart);
    }
}