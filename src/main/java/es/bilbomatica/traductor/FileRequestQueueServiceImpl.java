package es.bilbomatica.traductor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.traductor.exceptions.FileRequestQueueAtCapacityException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestWSInfo;

@Service
public class FileRequestQueueServiceImpl implements FileRequestQueueService {

    private final int QUEUE_MAX_CAPACITY_FILES = 50;
    
    private Map<UUID, FileRequest> fileRequests;
    private List<UUID> requestOrder;
    private Optional<Runnable> queueUpdatedCallback;


    private FileRequestQueueServiceImpl() {
        fileRequests = new ConcurrentHashMap<>();
        requestOrder = Collections.synchronizedList(new ArrayList<>());
        queueUpdatedCallback = Optional.empty();
    }


    @Override
    public UUID add(FileRequest request) throws FileRequestQueueAtCapacityException {
        UUID requestId = UUID.randomUUID();
        request.setId(Optional.of(requestId));

        synchronized(this) {
            if(fileRequests.size() >= QUEUE_MAX_CAPACITY_FILES) {
                throw new FileRequestQueueAtCapacityException(QUEUE_MAX_CAPACITY_FILES);
            }

            fileRequests.put(requestId, request);
            requestOrder.add(requestId);
            queueUpdatedCallback.ifPresent(Runnable::run);
        }

        return requestId;
    }


    @Override
    public void addAll(List<FileRequest> requests) throws FileRequestQueueAtCapacityException {
        synchronized(this) {
            for(FileRequest request : requests) {
                if(fileRequests.size() >= QUEUE_MAX_CAPACITY_FILES) {
                    throw new FileRequestQueueAtCapacityException(QUEUE_MAX_CAPACITY_FILES);
                }

                UUID requestId = UUID.randomUUID();
                request.setId(Optional.of(requestId));
                
                fileRequests.put(requestId, request);
                requestOrder.add(requestId);
            }
        }
        queueUpdatedCallback.ifPresent(Runnable::run);
    }


    @Override
    public FileRequest get(UUID requestId) {
        return this.fileRequests.get(requestId);
    }


    @Override
    public Optional<FileRequest> next() {
        Optional<FileRequest> ret;

        synchronized(this) {
            ret = this.requestOrder.stream()
                .map(fileRequests::get)
                .filter(r -> FileRequestStatus.IN_PROGRESS.equals(r.getStatus()))
                .findFirst()
                .or(() -> this.requestOrder.stream()
                    .map(fileRequests::get)
                    .filter(r -> FileRequestStatus.PENDING.equals(r.getStatus()))
                    .findFirst()
                );
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
    public List<FileRequest> removeCompleted() {
        List<FileRequest> ret = new ArrayList<>();
        synchronized(this) {
            List<UUID> requestsToRemove = fileRequests.entrySet().stream()
                .filter(e -> FileRequestStatus.DONE.equals(e.getValue().getStatus()))
                .map(e -> e.getKey())
                .toList();

            ret = fileRequests.entrySet().stream()
                .filter(e -> requestsToRemove.contains(e.getKey()))
                .map(e -> e.getValue())
                .toList();
    
            for(UUID id : requestsToRemove) {
                fileRequests.remove(id);
                requestOrder.remove(id);
            }

            queueUpdatedCallback.ifPresent(Runnable::run);
        }

        return ret;
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
    public List<FileRequest> getAllRequests() {
        return requestOrder.stream()
            .map(fileRequests::get)
            .toList();
    }


    @Override
    public List<FileRequestWSInfo> getAllRequestsInfo() {
        return requestOrder.stream()
            .map(fileRequests::get)
            .map(FileRequestWSInfo::from)
            .toList();
    }


    @Override
    public void onQueueUpdated(Runnable callback) {
        queueUpdatedCallback = Optional.of(callback);
    }

}
