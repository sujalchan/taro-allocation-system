package nz.ac.aut.comp713.customer_service.exception;

public class TaroTypeAlreadyExistsException extends RuntimeException {

    public TaroTypeAlreadyExistsException(String name) {
        super("Taro type '" + name + "' already exists.");
    }
}