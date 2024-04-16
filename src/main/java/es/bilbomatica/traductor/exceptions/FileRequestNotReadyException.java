package es.bilbomatica.traductor.exceptions;

public class FileRequestNotReadyException extends BusinessException {

    private String filename;
    
    public FileRequestNotReadyException(String filename) {
        super("El archivo " + filename + " aún no ha sido traducido.");
        this.filename = filename;
    }


    @Override
    public String getUserMessage() {
        return "El archivo " + this.filename + " aún no ha sido traducido.";
    }
}
