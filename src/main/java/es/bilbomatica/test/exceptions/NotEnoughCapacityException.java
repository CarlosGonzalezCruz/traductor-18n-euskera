package es.bilbomatica.test.exceptions;

public class NotEnoughCapacityException extends Exception {
    
    public NotEnoughCapacityException(int maximumCapacity, String exceedingPropertyValue) {
        super("El valor \"" + exceedingPropertyValue +"\" tiene un tamaño de " + exceedingPropertyValue.length()
        + " caracteres, que excede la capacidad permitida de " + maximumCapacity + " caracteres por bundle.");
    }
}
