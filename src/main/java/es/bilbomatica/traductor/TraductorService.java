package es.bilbomatica.traductor;

import es.bilbomatica.traductor.model.FileRequest;

public interface TraductorService {

    void translateFile(FileRequest fileRequest) throws InterruptedException;
}
