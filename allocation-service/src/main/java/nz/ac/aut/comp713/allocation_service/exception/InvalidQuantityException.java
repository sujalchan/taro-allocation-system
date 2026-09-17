package nz.ac.aut.comp713.allocation_service.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException() {
        super("Quantity must be a positive whole number");
    }
}