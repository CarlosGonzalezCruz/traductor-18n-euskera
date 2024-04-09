package es.bilbomatica.test.logic;

import java.io.BufferedInputStream;
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

import org.springframework.web.multipart.MultipartFile;

import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.bilbomatica.traductor.exceptions.WrongFormatException;

public class i18nJsonResourceFile implements i18nResourceFile {

    private final String UPDATE_NAME_FIND_REGEX = "^(.*)(?<=\\W|_)es(.*)?$";
    private final String UPDATE_NAME_REPLACE_REGEX = "$1eu$2";

    private Map<String, String> properties;
    private String name;

    private i18nJsonResourceFile(Map<String, String> properties, String name) {
        this.properties = properties;
        this.name = name;
    }

    public static i18nJsonResourceFile load(MultipartFile file) throws IOException, WrongFormatException {
        ObjectMapper mapper = new ObjectMapper();         
		InputStream stream = new BufferedInputStream(file.getInputStream());

        try {
            Map<?, ?> map = mapper.readValue(stream, Map.class);
            Map<String, String> flatMap = flattenHierarchy(map, "");
    
            return new i18nJsonResourceFile(flatMap, file.getOriginalFilename());

        } catch(JsonParseException e) {
            throw new WrongFormatException(file.getOriginalFilename(), I18nResourceFileType.JSON.getName());

        }
    }

    @Override
    public String getName() {
        return this.name;
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
    public void updateName() {
        this.name = name.replaceAll(UPDATE_NAME_FIND_REGEX, UPDATE_NAME_REPLACE_REGEX);
    }

    @Override
    public void writeToOutput(OutputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(stream, restoreHierarchy(properties));
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
