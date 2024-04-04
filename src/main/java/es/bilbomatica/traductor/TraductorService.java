package es.bilbomatica.traductor;

import es.bilbomatica.test.logic.i18nResourceFile;

public interface TraductorService {

    void translateFile(i18nResourceFile file) throws InterruptedException;
}
