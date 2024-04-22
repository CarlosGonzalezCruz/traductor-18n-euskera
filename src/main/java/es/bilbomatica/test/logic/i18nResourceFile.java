package es.bilbomatica.test.logic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public interface i18nResourceFile {
    
    String getName();

    Map<String, String> getProperties();

    int size();

    Optional<String> getProperty(String key);

    void updateProperties(Map<String, String> newProperties);

    String getTranslatedName();

    void writeToOutput(OutputStream stream) throws IOException;
}
