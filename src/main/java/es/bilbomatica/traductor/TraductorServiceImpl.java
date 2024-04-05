package es.bilbomatica.traductor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.traductor.controllers.ProgressControllerWS;
import es.bilbomatica.traductor.model.ProgressUpdate;
import es.bilbomatica.traductor.model.TraductorRequest;
import es.bilbomatica.traductor.model.TraductorResponse;

@Service
public class TraductorServiceImpl implements TraductorService {

	@Autowired
	private ProgressControllerWS progressControllerWS;

    private final static Long WAIT_BETWEEN_QUERIES_MS = 0L;

	private TraductorServiceImpl() {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}

    public void translateFile(i18nResourceFile file) throws InterruptedException {
		Map<String, String> properties = new HashMap<>(file.getProperties());
		int i = 0;
		for(Entry<String, String> property : properties.entrySet()) {
			System.out.print("[" + i + "/" + properties.size() + "] " + property.getValue() + " - ");
			String translatedValue = sendToTranslate(property.getValue());
			System.out.println(translatedValue);
			properties.put(property.getKey(), translatedValue);
			progressControllerWS.sendUpdate(new ProgressUpdate(i, properties.size(), false));
			Thread.sleep(WAIT_BETWEEN_QUERIES_MS);
			i++;
		}
		file.updateProperties(properties);
		file.updateName();
		progressControllerWS.sendUpdate(new ProgressUpdate(i, properties.size(), true));
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
