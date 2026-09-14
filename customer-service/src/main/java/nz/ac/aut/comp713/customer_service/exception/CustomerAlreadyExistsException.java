package nz.ac.aut.comp713.customer_service.exception;

public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(String name) {
        super("Customer" + name + " already exists.");
    }
}
