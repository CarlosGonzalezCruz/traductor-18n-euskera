package es.bilbomatica.test.logic;

import java.io.File;
import java.io.FileOutputStream;
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

public class i18nPropertiesResourceFile implements i18nResourceFile {

    private Map<String, String> properties;
    private String targetPath;

    private i18nPropertiesResourceFile(Map<String, String> properties, String targetPath) {
        this.properties = properties;
        this.targetPath = targetPath;
    }

    public static i18nPropertiesResourceFile load(String path, String targetPath) throws IOException {
        Properties properties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream(path);
		properties.load(new InputStreamReader(stream, Charset.forName("UTF-8")));

		Map<String, String> ret = new HashMap<>();
		for(String key : properties.stringPropertyNames()) {
			ret.put(key, properties.getProperty(key));
		}

        return new i18nPropertiesResourceFile(ret, targetPath);
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
        Properties prop = new Properties();
        prop.putAll(this.properties);
        prop.store(output, null);
        output.close();
    }
}
