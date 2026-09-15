package nz.ac.aut.comp713.allocation_service.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }
}