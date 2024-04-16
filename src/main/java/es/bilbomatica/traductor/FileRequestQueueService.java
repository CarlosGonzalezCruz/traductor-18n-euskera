package es.bilbomatica.traductor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import es.bilbomatica.traductor.exceptions.FileRequestNotReadyException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestInfo;

public interface FileRequestQueueService {

    public void add(FileRequest request);

    public FileRequest get(UUID requestId);

    public void downloadSource(UUID requestId, OutputStream outputStream) throws IOException;

    public void downloadTranslated(UUID requestId, OutputStream outputStream) throws FileRequestNotReadyException, IOException;
    
    public Optional<FileRequest> next();

    public void remove(UUID requestId);

    public void removeCompleted();

    public void rearrange(List<UUID> requestIds);

    public List<FileRequestInfo> getAllRequestsInfo();

    public void onQueueUpdated(Runnable callback);
}
