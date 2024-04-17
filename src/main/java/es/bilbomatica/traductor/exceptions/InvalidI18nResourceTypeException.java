package es.bilbomatica.traductor.exceptions;

public class InvalidI18nResourceTypeException extends BusinessException {

    private String invalidType;

    public InvalidI18nResourceTypeException(String invalidType) {
        super("El tipo de \"" + invalidType + "\" no se reconoce como un tipo de recurso de i18n.");
        this.invalidType = invalidType;
    }

    public String getUserMessage() {
        return "El tipo de \"" + invalidType + "\" no se reconoce como un tipo de recurso de i18n que se pueda traducir. "
        + "Por favor, envíe solo archivos con las extensiones autorizadas.";
    }
}
