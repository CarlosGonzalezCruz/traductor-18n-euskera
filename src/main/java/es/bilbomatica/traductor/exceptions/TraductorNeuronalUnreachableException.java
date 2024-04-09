package es.bilbomatica.traductor.exceptions;

public class TraductorNeuronalUnreachableException extends BusinessException {

    public TraductorNeuronalUnreachableException() {
        super("No se puede establecer la conexión con el Traductor Neuronal.");
    }

    public String getUserMessage() {
        return "No se puede establecer la conexión con el Traductor Neuronal.";
    }
}
