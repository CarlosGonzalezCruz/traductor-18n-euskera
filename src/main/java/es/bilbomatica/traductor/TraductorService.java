package es.bilbomatica.traductor;

import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.traductor.exceptions.TraductorNeuronalRateLimitException;
import es.bilbomatica.traductor.exceptions.TraductorNeuronalUnreachableException;

public interface TraductorService {

    void translateFile(i18nResourceFile file) throws InterruptedException, TraductorNeuronalRateLimitException, TraductorNeuronalUnreachableException;
}
