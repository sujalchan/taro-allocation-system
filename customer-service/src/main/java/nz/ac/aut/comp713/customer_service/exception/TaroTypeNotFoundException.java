package nz.ac.aut.comp713.customer_service.exception;

public class TaroTypeNotFoundException extends RuntimeException {
    public TaroTypeNotFoundException(Long id) {
        super("Taro type not found with id: " + id);
    }
}
