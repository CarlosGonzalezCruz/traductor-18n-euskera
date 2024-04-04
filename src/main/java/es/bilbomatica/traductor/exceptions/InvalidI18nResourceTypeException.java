package es.bilbomatica.traductor.exceptions;

public class InvalidI18nResourceTypeException extends Exception {

    public InvalidI18nResourceTypeException(String invalidType) {
        super("El tipo de recurso \"" + invalidType + "\" no se reconoce como un tipo de recurso de i18n.");
    }
}
