package es.bilbomatica.test.logic;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface i18nResourceFile {
    
    Map<String, String> getProperties();

    int size();

    Optional<String> getProperty(String key);

    void updateProperties(Map<String, String> newProperties);

    void save() throws IOException;
}
