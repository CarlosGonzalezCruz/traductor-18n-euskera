package es.bilbomatica.traductor;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.traductor.controllers.ProgressControllerWS;
import es.bilbomatica.traductor.exceptions.FileRequestQueueAtCapacityException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.persistence.FileLocation;
import es.bilbomatica.traductor.persistence.FileManagerService;
import jakarta.annotation.PostConstruct;

@Service
public class DirectorServiceImpl implements DirectorService {

    @Autowired
    private FileRequestQueueService fileRequestQueueService;

    @Autowired
    private TraductorService traductorService;

    @Autowired
    private ProgressControllerWS progressControllerWS;

    @Autowired
    private FileManagerService fileManagerService;

    private Lock internalThreadLock;
    private Condition queueNotEmpty;

    private Thread internalThread = new Thread() {
        @Override
        public void run() {
            internalThreadLock.lock();
                
            Optional<FileRequest> currentRequest = Optional.empty();
            
            while(!Thread.interrupted()) {

                try {
                    progressControllerWS.sendRequestList(fileRequestQueueService.getAllRequestsInfo());
                    currentRequest = fileRequestQueueService.next();

                    try {
                        fileManagerService.updateIndex(fileRequestQueueService.getAllRequests());
                    } catch (IOException e) {
                        System.err.println("No se ha podido actualizar el archivo del índice. Motivo: " + e.getLocalizedMessage());
                    }
                    
                    try {
                        if(currentRequest.isPresent()) {
                            traductorService.translateFile(currentRequest.get());
                            i18nResourceFile translatedData = currentRequest.get().getFileData();
                            
                            if(FileRequestStatus.DONE.equals(currentRequest.get().getStatus())) {
                                try {
                                    UUID translatedFileId = fileManagerService.save(translatedData.getTranslatedName(), FileLocation.TRANSLATED, translatedData::writeToOutput);
                                    currentRequest.get().setTranslatedFileId(Optional.of(translatedFileId));
                                } catch(IOException e) {
                                    currentRequest.get().setStatus(FileRequestStatus.ERROR);
                                    currentRequest.get().setErrorMessage(Optional.of("No se ha podido guardar el archivo " + translatedData.getName() + " traducido."));
                                }
                            }
                            currentRequest.get().releaseFileData();
                            
                        } else {
                            queueNotEmpty.await();
                            
                        }
                        
                    } catch(InterruptedException e) {
                        internalThreadLock.unlock();
                        Thread.currentThread().interrupt();
                        
                    } catch(Exception e) {
                        if(currentRequest.isPresent()) {
                            currentRequest.get().setStatus(FileRequestStatus.ERROR);
                            currentRequest.get().setErrorMessage(Optional.of("Ha ocurrido un problema interno inesperado."));
                        }
                        e.printStackTrace();
                    }
                    
                } catch(Exception e) {
                    e.printStackTrace();
                }
                
            }      
        }
    };

    private DirectorServiceImpl() {
        this.internalThreadLock = new ReentrantLock(true);
        this.queueNotEmpty = internalThreadLock.newCondition();
    }

    @Override
    @PostConstruct
    public void start() {
        this.fileRequestQueueService.onQueueUpdated(
            () -> progressControllerWS.sendRequestList(fileRequestQueueService.getAllRequestsInfo())
        );
        
        try {
            this.fileRequestQueueService.addAll(this.fileManagerService.loadIndex());
        } catch (FileRequestQueueAtCapacityException | IOException e) {
            System.err.println("No se ha podido cargar el índice de archivos. Motivo: " + e.getLocalizedMessage());
        }

        try {
            this.fileManagerService.clearOrphans();
        } catch (IOException e) {
            System.err.println("No se ha podido limpiar los directorios. Motivo: " + e.getLocalizedMessage());
        }

        this.internalThread.start();
    }

    @Override
    public void resume() {
        try {
            fileManagerService.updateIndex(fileRequestQueueService.getAllRequests());
        } catch (IOException e) {
            System.err.println("No se ha podido actualizar el archivo del índice. Motivo: " + e.getLocalizedMessage());
        }

        if(internalThreadLock.tryLock()) {
            queueNotEmpty.signal();
            internalThreadLock.unlock();
        }
    }

}
