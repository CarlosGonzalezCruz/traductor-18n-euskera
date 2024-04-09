package es.bilbomatica.traductor.exceptions;

public class TraductorNeuronalRateLimitException extends BusinessException {

    public TraductorNeuronalRateLimitException() {
        super("El Traductor Neuronal ha dejado de responder por exceso de tráfico.");
    }

    public String getUserMessage() {
        return "El Traductor Neuronal ha dejado de responder por exceso de tráfico.";
    }
}
