package es.bilbomatica.traductor.exceptions;


public class FileRequestQueueAtCapacityException extends BusinessException {


    public FileRequestQueueAtCapacityException(int capacity) {
        super("No se puede agregar otro archivo porque se ha alcanzado el límite de " + capacity + " elementos.");
    }


    public String getUserMessage() {
        return "La cola está llena. Elimine algunos archivos e inténtelo de nuevo.";
    }
}
