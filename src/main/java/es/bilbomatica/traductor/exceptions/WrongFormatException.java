package es.bilbomatica.traductor.exceptions;

public class WrongFormatException extends BusinessException {

    private String filename;
    private String expectedFormat;

    public WrongFormatException(String filename, String expectedFormat) {
        super("El archivo " + filename + " no se ha podido interpretar con el formato " + expectedFormat + ".");
        this.filename = filename;
        this.expectedFormat = expectedFormat;
    }

    public String getUserMessage() {
        return "El archivo " + filename + " no se ha podido interpretar con el formato " + expectedFormat + ".";
    }
}
