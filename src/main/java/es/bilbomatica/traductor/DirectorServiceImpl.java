package es.bilbomatica.traductor;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bilbomatica.traductor.controllers.ProgressControllerWS;
import es.bilbomatica.traductor.model.FileRequest;
import jakarta.annotation.PostConstruct;

@Service
public class DirectorServiceImpl implements DirectorService {

    @Autowired
    private FileRequestQueueService fileRequestQueueService;

    @Autowired
    private TraductorService traductorService;

    @Autowired
    private ProgressControllerWS progressControllerWS;

    private Lock internalThreadLock;
    private Condition queueNotEmpty;

    private Thread internalThread = new Thread() {
        @Override
        public void run() {
            internalThreadLock.lock();
                
            Optional<FileRequest> currentRequest = Optional.empty();
            
            while(!Thread.interrupted()) {
                
                progressControllerWS.sendRequestList(fileRequestQueueService.getAllRequestsInfo());
                currentRequest = fileRequestQueueService.next();
                
                try {
                    if(currentRequest.isPresent()) {
                        traductorService.translateFile(currentRequest.get());
                        
                    } else {
                        queueNotEmpty.await();
                        
                    }
                    
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    
                }
                
            }      
        }
    };

    private DirectorServiceImpl() {
        this.internalThreadLock = new ReentrantLock();
        this.queueNotEmpty = internalThreadLock.newCondition();
    }

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
        if(internalThreadLock.tryLock()) {
            queueNotEmpty.signal();
            internalThreadLock.unlock();
        }
    }

}
