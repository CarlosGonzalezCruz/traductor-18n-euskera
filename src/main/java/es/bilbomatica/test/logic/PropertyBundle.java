package es.bilbomatica.test.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PropertyBundle {

    private final static String SEPARATOR = "§";
    
    private Map<String, Integer> keys;
    private StringBuilder sb;
    private int size;
    private int capacity;

    public PropertyBundle(int capacity) {
        keys = new HashMap<>();
        sb = new StringBuilder();
        size = 0;
        this.capacity = capacity;
    }

    public static PropertyBundle fromReplacing(PropertyBundle source, String newRawText) {
        int newSize = newRawText.length() - newRawText.replace(SEPARATOR, "").length();
        if(newSize != source.size) {
            System.err.println("El nuevo texto del bundle no tiene el mismo número de separadores (" + newSize
            + ") que el original (" + source.size + "). Probablemente surjan errores al leer las propiedades.");
        }

        PropertyBundle ret = new PropertyBundle(source.capacity);
        ret.sb.replace(0, newRawText.length(), newRawText);
        ret.keys = source.keys;
        ret.size = newSize;
        return ret;
    }
    
    public boolean tryAddProperty(String key, String value) {
        if(sb.length() + value.length() > capacity) {
            return false;
        }

        keys.put(key, size++);
        sb.append(SEPARATOR);
        sb.append(value);
        return true;
    }

    public String getProperty(String key) {
        if(!this.keys.containsKey(key)) {
            return null;
        }

        return this.getAllProperties().get(key);
    }

    public Map<String, String> getAllProperties() {
        String[] separatedStrings = sb.toString().split(SEPARATOR.toString());
        Map<String, String> ret = new HashMap<>();
        for(Entry<String, Integer> keyIndex : this.keys.entrySet()) {
            ret.put(keyIndex.getKey(), separatedStrings[keyIndex.getValue()+1].trim());
        }
        return ret;
    }

    public String getRawText() {
        return sb.toString();
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
