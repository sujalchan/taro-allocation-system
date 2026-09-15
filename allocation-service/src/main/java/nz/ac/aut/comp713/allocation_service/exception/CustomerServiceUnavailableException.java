package nz.ac.aut.comp713.allocation_service.exception;

public class CustomerServiceUnavailableException extends RuntimeException {

    public CustomerServiceUnavailableException() {
        super("Customer service is currently unavailable");
    }
}