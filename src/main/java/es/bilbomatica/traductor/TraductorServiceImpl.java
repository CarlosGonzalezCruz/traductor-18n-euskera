package es.bilbomatica.traductor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.traductor.controllers.ProgressControllerWS;
import es.bilbomatica.traductor.exceptions.BusinessException;
import es.bilbomatica.traductor.exceptions.TraductorNeuronalRateLimitException;
import es.bilbomatica.traductor.exceptions.TraductorNeuronalUnreachableException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.ProgressUpdate;
import es.bilbomatica.traductor.model.TraductorRequest;
import es.bilbomatica.traductor.model.TraductorResponse;

@Service
public class TraductorServiceImpl implements TraductorService {

	@Autowired
	private ProgressControllerWS progressControllerWS;

    private final static Long WAIT_BETWEEN_QUERIES_MS = 0L;
	private final static Long WAIT_IF_ERROR_MS = 1000L;
	private final static Long TRADUCTOR_NEURONAL_TIMEOUT_MS = 5000L;

	private TraductorServiceImpl() {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}

    public void translateFile(FileRequest fileRequest) throws InterruptedException {
		
		UUID requestId = fileRequest.getId().orElseThrow(
			() -> new IllegalArgumentException("No se permite procesar un FileRequest sin id.")
		);

		fileRequest.setStatus(FileRequestStatus.IN_PROGRESS);
		i18nResourceFile file = fileRequest.getFileData();

		Map<String, String> properties = new HashMap<>(file.getProperties());
		long lastUpdateTimeNS = System.nanoTime();
		List<Long> timePerUpdateNS = new ArrayList<>();
		int i = 0;

		ProgressUpdate update = new ProgressUpdate(
			requestId, fileRequest.getStatus(), 0, properties.size(), false, Optional.empty()
		);
		fileRequest.setLastProgressUpdate(Optional.of(update));
		progressControllerWS.sendUpdate(update);

		for(Entry<String, String> property : properties.entrySet()) {

			if(FileRequestStatus.CANCELLED.equals(fileRequest.getStatus())) {
				break;
			}
			
			try {
				String translatedValue = sendToTranslate(property.getValue());
				properties.put(property.getKey(), translatedValue);
				
				timePerUpdateNS.add(System.nanoTime() - lastUpdateTimeNS);
				lastUpdateTimeNS = System.nanoTime();
				long remainingTimeNS = calculateRemainingTimeNS(timePerUpdateNS, properties.size());
				
				update = new ProgressUpdate(
					requestId, fileRequest.getStatus(), i, properties.size(), false, Optional.of(remainingTimeNS)
				);
				fileRequest.setLastProgressUpdate(Optional.of(update));
				progressControllerWS.sendUpdate(update);
				
				Thread.sleep(WAIT_BETWEEN_QUERIES_MS);
				i++;
			
			} catch(BusinessException e) {
				fileRequest.setStatus(FileRequestStatus.ERROR);
				fileRequest.setErrorMessage(Optional.of(e.getUserMessage()));
				Thread.sleep(WAIT_IF_ERROR_MS); // Si el error era por exceso de tráfico, esperamos un poco antes
												// de devolver el control al caller, por si acaso el caller decide
												// empezar la traducción de otro archivo inmediatamente.
				break;

			}
		}
		
		if(!FileRequestStatus.CANCELLED.equals(fileRequest.getStatus()) && !FileRequestStatus.ERROR.equals(fileRequest.getStatus())) {
			file.updateProperties(properties);
			fileRequest.setStatus(FileRequestStatus.DONE);
		}

		update = new ProgressUpdate(
			requestId, fileRequest.getStatus(), i, properties.size(), true, Optional.empty()
		);
		fileRequest.setLastProgressUpdate(Optional.of(update));
		progressControllerWS.sendUpdate(update);
	}

	@SuppressWarnings("null")
	private static String sendToTranslate(String content) throws TraductorNeuronalRateLimitException, TraductorNeuronalUnreachableException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Origin", "https://www.euskadi.eus");

		TraductorRequest request = new TraductorRequest();
		request.setMkey("8d9016025eb0a44215c7f69c2e10861d");
		request.setModel("generic_es2eu");
		request.setText(content);

		HttpEntity<TraductorRequest> entity = new HttpEntity<>(request, headers);
		RestTemplate rest = new RestTemplateBuilder()
			.setConnectTimeout(Duration.ofMillis(TRADUCTOR_NEURONAL_TIMEOUT_MS))
			.setReadTimeout(Duration.ofMillis(TRADUCTOR_NEURONAL_TIMEOUT_MS))
			.build();

		try {
			ResponseEntity<TraductorResponse> response = rest.exchange("https://api.euskadi.eus/itzuli/es2eu/translate",
					HttpMethod.POST, entity, TraductorResponse.class);
	
			return response.getBody().getMessage();
		
		} catch(TooManyRequests e) {
			throw new TraductorNeuronalRateLimitException();
			
		} catch(HttpClientErrorException | ResourceAccessException e) {
			throw new TraductorNeuronalUnreachableException();
			
		}
	}

	private long calculateRemainingTimeNS(List<Long> timePerUpdateNS, int total) {
		long averageTimePerUpdateNS = timePerUpdateNS.stream().reduce(0L, Long::sum) / timePerUpdateNS.size();
		int remaining = total - timePerUpdateNS.size();

		return remaining * (averageTimePerUpdateNS + WAIT_BETWEEN_QUERIES_MS);
	}
}
