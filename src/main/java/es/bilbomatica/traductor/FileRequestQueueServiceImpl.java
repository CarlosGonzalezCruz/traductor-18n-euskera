package es.bilbomatica.traductor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.traductor.exceptions.FileRequestNotReadyException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestInfo;

@Service
public class FileRequestQueueServiceImpl implements FileRequestQueueService {

    private Map<UUID, FileRequest> fileRequests;
    private List<UUID> requestOrder;
    private Optional<Runnable> queueUpdatedCallback;


    private FileRequestQueueServiceImpl() {
        fileRequests = new ConcurrentHashMap<>();
        requestOrder = Collections.synchronizedList(new ArrayList<>());
        queueUpdatedCallback = Optional.empty();
    }


    @Override
    public void add(FileRequest request) {
        UUID requestId = UUID.randomUUID();
        request.setId(Optional.of(requestId));

        synchronized(this) {
            fileRequests.put(requestId, request);
            requestOrder.add(requestId);
            queueUpdatedCallback.ifPresent(Runnable::run);
        }
    }


    @Override
    public FileRequest get(UUID requestId) {
        return this.fileRequests.get(requestId);
    }


    @Override
    public void downloadSource(UUID requestId, OutputStream outputStream) throws IOException {
        outputStream.write(this.fileRequests.get(requestId).getOriginalFile().getBytes());
    }


    @Override
    public void downloadTranslated(UUID requestId, OutputStream outputStream) throws FileRequestNotReadyException, IOException {
        FileRequest fileRequest = this.fileRequests.get(requestId);
        if(!FileRequestStatus.DONE.equals(fileRequest.getStatus())) {
            throw new FileRequestNotReadyException(fileRequest.getSourceName());
        }
        fileRequest.getResourceFile().writeToOutput(outputStream);
    }


    @Override
    public Optional<FileRequest> next() {
        Optional<FileRequest> ret = Optional.empty();

        synchronized(this) {
            for(UUID requestId : this.requestOrder) {
                
                FileRequest currentRequest = this.fileRequests.get(requestId);
                
                if(FileRequestStatus.PENDING.equals(currentRequest.getStatus())) {
                    ret = Optional.of(currentRequest);
                    break;
                    
                } else {
                    continue;
                }
            }
        }

        return ret;
    }


    @Override
    public void remove(UUID requestId) {
        synchronized(this) {
            this.fileRequests.remove(requestId);
            this.requestOrder.remove(requestId);
            queueUpdatedCallback.ifPresent(Runnable::run);
        }
    }


    @Override
    public void removeCompleted() {
        synchronized(this) {
            List<UUID> requestsToRemove = fileRequests.entrySet().stream()
                .filter(e -> FileRequestStatus.DONE.equals(e.getValue().getStatus()))
                .map(e -> e.getKey())
                .toList();
    
            for(UUID id : requestsToRemove) {
                fileRequests.remove(id);
                requestOrder.remove(id);
            }

            queueUpdatedCallback.ifPresent(Runnable::run);
        }
    }


    @Override
    public void rearrange(List<UUID> requestIds) {
        synchronized(this) {
            // Quitamos los ids que no corresponden a requests. Después de eso, sii la lista solo tenía ids válidos,
            // tras este filtro seguirá teniendo al menos el mismo tamaño que la lista de requests.
    
            List<UUID> filteredIds = requestIds.stream().filter(id -> fileRequests.containsKey(id)).toList();
            if(filteredIds.size() != fileRequests.size()) {
                throw new IllegalArgumentException("El nuevo orden recibido no tiene los mismos elementos que el viejo orden.");
            }
            
            this.requestOrder = filteredIds;
            queueUpdatedCallback.ifPresent(Runnable::run);
        }
    }


    @Override
    public List<FileRequestInfo> getAllRequestsInfo() {
        return requestOrder.stream()
            .map(id -> fileRequests.get(id))
            .map(FileRequestInfo::from)
            .toList();
    }


    @Override
    public void onQueueUpdated(Runnable callback) {
        queueUpdatedCallback = Optional.of(callback);
    }

}
