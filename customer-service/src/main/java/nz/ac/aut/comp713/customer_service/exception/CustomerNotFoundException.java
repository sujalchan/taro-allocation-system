package nz.ac.aut.comp713.customer_service.exception;

/**
 * CustomerNotFoundException
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }

}
