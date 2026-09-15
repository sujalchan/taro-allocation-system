package nz.ac.aut.comp713.allocation_service.exception;

public class DuplicateTaroTypeException extends RuntimeException {

    public DuplicateTaroTypeException(Long taroTypeId) {
        super("Taro type " + taroTypeId + " cannot appear more than once in an allocation");
    }
}