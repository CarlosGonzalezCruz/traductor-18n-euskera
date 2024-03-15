package es.bilbomatica.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import es.bilbomatica.test.exceptions.NotEnoughCapacityException;
import es.bilbomatica.test.logic.PropertyBundle;
import es.bilbomatica.test.model.TraductorRequest;
import es.bilbomatica.test.model.TraductorResponse;

public class TestTraductor {

	private final static String PROPERTIES_RESOURCE_PATH = "static/ae18b.i18n_es.properties";
	private final static Integer CHARACTER_LIMIT_PER_BUNDLE = 2000;
	private final static Long WAIT_BETWEEN_BUNDLES_MS = 2000L;

    public static void main(String[] args) throws IOException, InterruptedException, NotEnoughCapacityException {

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		Map<String, String> properties = getProperties();
		List<PropertyBundle> bundles = generateBundles(properties, CHARACTER_LIMIT_PER_BUNDLE);
		
		long expectedWaitTimeSeconds = bundles.size() * WAIT_BETWEEN_BUNDLES_MS / 1000L;
		System.out.println("Traduciendo, por favor espere al menos " + expectedWaitTimeSeconds + " segundos...");
		List<PropertyBundle> translatedBundles = sendBundles(bundles);
		Map<String, String> translatedProperties = combineBundles(translatedBundles);

		for(String property : properties.keySet()) {
			System.out.println(properties.get(property) + " - " + translatedProperties.get(property));
		}

		System.out.println();
        
    }

	private static Map<String, String> getProperties() throws IOException {
		Properties properties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream(PROPERTIES_RESOURCE_PATH);
		properties.load(new InputStreamReader(stream, Charset.forName("UTF-8")));

		Map<String, String> ret = new HashMap<>();
		for(String key : properties.stringPropertyNames()) {
			ret.put(key, properties.getProperty(key));
		}
		return ret;
	}

	private static List<PropertyBundle> generateBundles(Map<String, String> properties, int characterLimit) throws NotEnoughCapacityException {
		List<PropertyBundle> ret = new ArrayList<>();
		PropertyBundle currentBundle = new PropertyBundle(characterLimit);
		for(Entry<String, String> property : properties.entrySet()) {
			boolean attempt = currentBundle.tryAddProperty(property.getKey(), property.getValue());
			if(!attempt) { // currentBundle está lleno. Guardar y empezar a llenar el siguiente.
				ret.add(currentBundle);
				currentBundle = new PropertyBundle(characterLimit);
				attempt = currentBundle.tryAddProperty(property.getKey(), property.getValue());
				if(!attempt) { // Si property ni siquiera cabe en bundle vacío, no se puede guardar.
					throw new NotEnoughCapacityException(characterLimit, property.getValue());
				}
			}
		}
		ret.add(currentBundle); // El último bundle probablemente no se habrá llenado del todo, lo guardamos igual.
		return ret;
	}

	private static List<PropertyBundle> sendBundles(List<PropertyBundle> sourceBundles) throws InterruptedException {
		List<PropertyBundle> ret = new ArrayList<>();
		for(PropertyBundle bundle : sourceBundles) {
			String translatedText = sendToTranslate(bundle.getRawText());
			PropertyBundle translatedBundle = PropertyBundle.fromReplacing(bundle, translatedText);
			ret.add(translatedBundle);
			Thread.sleep(WAIT_BETWEEN_BUNDLES_MS);
		}
		return ret;
	}

	private static String sendToTranslate(String content) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Origin", "https://www.euskadi.eus");

		TraductorRequest request = new TraductorRequest();
		request.setMkey("8d9016025eb0a44215c7f69c2e10861d");
		request.setModel("generic_es2eu");
		request.setText(content);

		HttpEntity<TraductorRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<TraductorResponse> response = new RestTemplate().exchange("https://api.euskadi.eus/itzuli/es2eu/translate",
				HttpMethod.POST, entity, TraductorResponse.class);

		return response.getBody().getMessage();
	}

	private static Map<String, String> combineBundles(List<PropertyBundle> bundles) {
		Map<String, String> properties = new HashMap<>();
		for(PropertyBundle bundle : bundles) {
			bundle.getAllProperties().forEach(properties::put);
		}
		return properties;
	}

    private static List<String> getFrases() {
		return Arrays.asList("contra techo perro el jardín brilla en", "techo en contra corre jardín Un la",
				"gato lee la Alguna en los perro", "techo Esa el Alguna es lee en", "corre en libro lee sol Alguna jardín",
				"salta la lee Cada brilla Alguna El", "parque Esta cielo debajo es Cada Un", "techo cielo luna El el perro casa",
				"la sobre luna Ninguna Cada salta Un", "Un sol techo la contra Esa los", "parque casa Esa luna lee el en",
				"salta techo El las el luna casa", "corre jardín Alguna sol brilla contra las", "salta los Cada Alguna debajo la corre",
				"gato perro las Esta el casa libro", "la en El luna sobre es Esta", "techo Ninguna en Un el jardín Esa",
				"sobre las corre parque Un perro luna", "El gato sobre lee el techo sol", "Alguna libro lee techo cielo el corre",
				"Cada es libro Un luna perro en", "gato cielo Esa en corre el los", "El en el Un contra brilla salta",
				"corre libro Alguna es casa la Cada", "Ninguna debajo los sobre parque las es", "debajo Esta sol Esa Un cielo en",
				"cielo luna parque El gato salta Esa", "salta luna en techo libro Ninguna El", "brilla debajo Ninguna es jardín lee las",
				"sol es Un sobre parque Esa perro", "lee la Un sol salta los Esta", "El corre el lee sobre cielo Esta",
				"Esa salta lee es debajo Ninguna cielo", "luna perro los es libro lee El", "la salta techo los es jardín parque",
				"cielo brilla parque Esa el gato en", "es cielo gato luna casa la Un", "techo cielo las Alguna Cada Un corre",
				"Ninguna Cada luna libro Un sobre corre", "corre contra lee Cada salta en sol", "debajo Un brilla Cada los corre en",
				"debajo parque Alguna corre gato sol Esa", "Esa techo debajo libro cielo las brilla", "corre parque luna Esa en los sobre",
				"Esa Esta corre casa sobre techo El", "las debajo gato corre brilla Alguna Cada", "perro parque gato Alguna jardín El es",
				"Alguna sobre Esa salta lee gato casa", "corre Alguna lee Cada las debajo luna", "los Alguna brilla jardín el Esta sobre");
	}
    
}
