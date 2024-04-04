package es.bilbomatica.test.logic;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.springframework.web.multipart.MultipartFile;

public class i18nPropertiesResourceFile implements i18nResourceFile {

    private final String UPDATE_NAME_FIND_REGEX = "^(.*)(?<=\\W|_)es(.*)?$";
    private final String UPDATE_NAME_REPLACE_REGEX = "$1eu$2";

    private Map<String, String> properties;
    private String name;

    private i18nPropertiesResourceFile(Map<String, String> properties, String name) {
        this.properties = properties;
        this.name = name;
    }

    public static i18nPropertiesResourceFile load(MultipartFile file) throws IOException {
        Properties properties = new Properties();         
		InputStream stream = new BufferedInputStream(file.getInputStream());
		properties.load(new InputStreamReader(stream, Charset.forName("UTF-8")));

		Map<String, String> ret = new HashMap<>();
		for(String key : properties.stringPropertyNames()) {
			ret.put(key, properties.getProperty(key));
		}

        return new i18nPropertiesResourceFile(ret, file.getOriginalFilename());
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
        Properties prop = new Properties();
        prop.putAll(this.properties);
        prop.store(stream, null);
    }
}
