package es.bilbomatica.traductor.exceptions;

public class InvalidI18nResourceTypeException extends BusinessException {

    private String invalidType;

    public InvalidI18nResourceTypeException(String invalidType) {
        super("El tipo de recurso \"" + invalidType + "\" no se reconoce como un tipo de recurso de i18n.");
        this.invalidType = invalidType;
    }

    public String getUserMessage() {
        return "El tipo de recurso \"" + invalidType + "\" no se reconoce como un tipo de recurso de i18n que se pueda traducir.";
    }
}
