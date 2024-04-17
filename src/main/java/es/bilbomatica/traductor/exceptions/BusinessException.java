package es.bilbomatica.traductor.exceptions;


public abstract class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public abstract String getUserMessage();
}
