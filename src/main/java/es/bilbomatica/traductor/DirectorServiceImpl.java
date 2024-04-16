package es.bilbomatica.traductor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bilbomatica.traductor.controllers.ProgressControllerWS;
import es.bilbomatica.traductor.model.FileRequest;
import jakarta.annotation.PostConstruct;

@Service
public class DirectorServiceImpl implements DirectorService {

    private final long AUTO_RESUME_WAIT_MS = 60000L;

    @Autowired
    private FileRequestQueueService fileRequestQueueService;

    @Autowired
    private TraductorService traductorService;

    @Autowired
    private ProgressControllerWS progressControllerWS;

    private Thread internalThread = new Thread() {
        @Override
        public void run() {
            synchronized(this) {
                
                Optional<FileRequest> currentRequest = Optional.empty();
                
                while(!Thread.interrupted()) {
                    
                    progressControllerWS.sendRequestList(fileRequestQueueService.getAllRequestsInfo());
                    currentRequest = fileRequestQueueService.next();
                    
                    try {
                        if(currentRequest.isPresent()) {
                            traductorService.translateFile(currentRequest.get());
                            
                        } else {
                            this.wait(AUTO_RESUME_WAIT_MS);
                            
                        }
                        
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        
                    }
                    
                }
            }
            
        }
    };

    private DirectorServiceImpl() {};

    @Override
    @PostConstruct
    public void start() {
        this.fileRequestQueueService.onQueueUpdated(
            () -> progressControllerWS.sendRequestList(fileRequestQueueService.getAllRequestsInfo())
        );
        this.internalThread.start();
    }

    @Override
    public void resume() {
        synchronized(internalThread) {
            internalThread.notify();
        }
    }

}
