package es.bilbomatica.traductor.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.util.function.ThrowingConsumer;

import es.bilbomatica.traductor.model.FileRequest;

public interface FileManagerService {

    public UUID save(String filename, FileLocation location, ThrowingConsumer<OutputStream> dataProvider) throws IOException;

    public boolean exists(UUID fileId);

    public InputStream load(UUID fileId) throws IOException;

    public void delete(UUID fileId) throws IOException;

    public void updateIndex(List<FileRequest> fileRequests) throws IOException;

    public List<FileRequest> loadIndex() throws IOException;

    public void clearOrphans() throws IOException;
}
