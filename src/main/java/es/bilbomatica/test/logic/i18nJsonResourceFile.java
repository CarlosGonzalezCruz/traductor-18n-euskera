package es.bilbomatica.test.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class i18nJsonResourceFile implements i18nResourceFile {

    private Map<String, String> properties;
    private String sourcePath;
    private String targetPath;

    private i18nJsonResourceFile(Map<String, String> properties, String sourcePath, String targetPath) {
        this.properties = properties;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    public static i18nJsonResourceFile load(String sourcePath, String targetPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream(sourcePath);
        Map<?, ?> map = mapper.readValue(stream, Map.class);
        Map<String, String> flatMap = flattenHierarchy(map, "");

		return new i18nJsonResourceFile(flatMap, sourcePath, targetPath);
    }

    @Override
    public String getName() {
        return this.sourcePath;
    }

    @Override
    public int size() {
        return this.properties.size();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(this.properties.get(key));
    }

    @Override
    public void updateProperties(Map<String, String> newProperties) {
        this.properties.clear();
        this.properties.putAll(newProperties);
    }

    @Override
    public void save() throws IOException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(".").getFile() + "/" + this.targetPath);
        file.createNewFile(); // Por si no existe

        OutputStream output = new FileOutputStream(file);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(output, restoreHierarchy(properties));
        output.close();
    }

    private static Map<String, String> flattenHierarchy(Map<?, ?> properties, String keyPrefix) {
        Map<String, String> ret = new HashMap<>();
        for(Entry<?, ?> entry : properties.entrySet()) {
            if(entry.getValue() instanceof Map) {
                ret.putAll(flattenHierarchy((Map<?, ?>) entry.getValue(), keyPrefix + entry.getKey() + "."));
            } else {
                ret.put(keyPrefix + entry.getKey(), entry.getValue().toString());
            }
        }
        return ret;
    }

    private static Map<String, Object> restoreHierarchy(Map<String, String> properties) {
        Map<String, Object> ret = new HashMap<>();
        for(Entry<String, String> property : properties.entrySet()) {
            storeInMapRecursively(property.getKey(), property.getValue(), ret);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static <T> void storeInMapRecursively(String recursiveKey, T value, Map<String, Object> map) {
        List<String> splitKey = new ArrayList<>(Arrays.asList(recursiveKey.split("\\.")));
        if(splitKey.size() == 0) {
            return;
        }
        String lastKey = splitKey.get(splitKey.size() - 1);
        splitKey.remove(splitKey.size() - 1);

        Map<String, Object> current = map;
        for(String key : splitKey) {
            if(!current.containsKey(key)) {
                current.put(key, new HashMap<>());
            }
            current =  (Map<String, Object>) current.get(key);
        }

        current.put(lastKey, value);
    }
}
