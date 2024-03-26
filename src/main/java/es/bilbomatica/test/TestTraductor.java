package es.bilbomatica.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import es.bilbomatica.test.exceptions.NotEnoughCapacityException;
import es.bilbomatica.test.logic.i18nJsonResourceFile;
import es.bilbomatica.test.logic.i18nPropertiesResourceFile;
import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.test.logic.i18nXMLResourceFile;
import es.bilbomatica.test.model.TraductorRequest;
import es.bilbomatica.test.model.TraductorResponse;

public class TestTraductor {

	private final static String PROPERTIES_RESOURCE_PATH_ES = "static/ad50bWebApp.i18n_es.properties";
	private final static String PROPERTIES_RESOURCE_PATH_EU = "static/ad50bWebApp.i18n_eu.properties";
	private final static String JSON_RESOURCE_PATH_ES = "static/ad50bWebApp.i18n_es.json";
	private final static String JSON_RESOURCE_PATH_EU = "static/ad50bWebApp.i18n_eu.json";
	private final static String XML_RESOURCE_PATH = "static/1057501.xml";
	private final static Long WAIT_BETWEEN_QUERIES_MS = 0L;

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException, XPathExpressionException {

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		i18nResourceFile[] files = new i18nResourceFile[] {
			i18nPropertiesResourceFile.load(PROPERTIES_RESOURCE_PATH_ES, PROPERTIES_RESOURCE_PATH_EU),
			i18nJsonResourceFile.load(JSON_RESOURCE_PATH_ES, JSON_RESOURCE_PATH_EU),
			i18nXMLResourceFile.load(XML_RESOURCE_PATH)
		};

		for(i18nResourceFile file : files) {
			System.out.println("Traduciendo el archivo '" + file.getName() + "''...");
			translateFile(file);
			file.save();
		}

		System.out.println("Hecho.");
	}

	private static void translateFile(i18nResourceFile file) throws InterruptedException {
		Map<String, String> properties = new HashMap<>(file.getProperties());
		int i = 0;
		for(Entry<String, String> property : properties.entrySet()) {
			System.out.print("[" + ++i + "/" + properties.size() + "] " + property.getValue() + " - ");
			String translatedValue = sendToTranslate(property.getValue());
			System.out.println(translatedValue);
			properties.put(property.getKey(), translatedValue);
			Thread.sleep(WAIT_BETWEEN_QUERIES_MS);
		}
		file.updateProperties(properties);
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
    
}
