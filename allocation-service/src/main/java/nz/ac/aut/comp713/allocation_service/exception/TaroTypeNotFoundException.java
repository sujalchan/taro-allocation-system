package nz.ac.aut.comp713.allocation_service.exception;

public class TaroTypeNotFoundException extends RuntimeException {

    public TaroTypeNotFoundException(Long taroTypeId) {
        super("Taro type not found with id: " + taroTypeId);
    }
}