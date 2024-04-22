package es.bilbomatica.traductor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import es.bilbomatica.traductor.exceptions.FileRequestQueueAtCapacityException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestWSInfo;

public interface FileRequestQueueService {

    public UUID add(FileRequest request) throws FileRequestQueueAtCapacityException;

    public void addAll(List<FileRequest> requests) throws FileRequestQueueAtCapacityException;

    public FileRequest get(UUID requestId);
    
    public Optional<FileRequest> next();

    public void remove(UUID requestId);

    public List<FileRequest> removeCompleted();

    public void rearrange(List<UUID> requestIds);

    public List<FileRequest> getAllRequests();

    public List<FileRequestWSInfo> getAllRequestsInfo();

    public void onQueueUpdated(Runnable callback);
}
