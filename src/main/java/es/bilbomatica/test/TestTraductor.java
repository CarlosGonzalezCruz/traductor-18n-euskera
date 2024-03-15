package es.bilbomatica.test;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import es.bilbomatica.test.model.TraductorRequest;
import es.bilbomatica.test.model.TraductorResponse;

public class TestTraductor {

    public static void main(String[] args) {

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		List<String> frases = getFrases();

		for (String frase : frases) {

			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Origin", "https://www.euskadi.eus");

			TraductorRequest request = new TraductorRequest();
			request.setMkey("8d9016025eb0a44215c7f69c2e10861d");
			request.setModel("generic_es2eu");
			request.setText(frase);

			HttpEntity<TraductorRequest> entity = new HttpEntity<>(request, headers);

			ResponseEntity<TraductorResponse> response = new RestTemplate().exchange("https://api.euskadi.eus/itzuli/es2eu/translate",
					HttpMethod.POST, entity, TraductorResponse.class);

			System.out.println(frase + " - " + response.getBody().getMessage());
		}
        
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
