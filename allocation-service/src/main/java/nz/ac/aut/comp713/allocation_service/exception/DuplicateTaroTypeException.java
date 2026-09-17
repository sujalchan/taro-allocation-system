package nz.ac.aut.comp713.allocation_service.exception;

public class DuplicateTaroTypeException extends RuntimeException {

    public DuplicateTaroTypeException(Long taroTypeId, String taroTypeName) {
        super(taroTypeName + " cannot appear more than once in an allocation");
    }
}