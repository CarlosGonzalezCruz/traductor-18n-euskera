package es.bilbomatica.traductor.model;

import java.util.Optional;
import java.util.UUID;

import es.bilbomatica.test.logic.FileRequestStatus;

public class FileRequestInfo {

    private Optional<UUID> id;
    private FileRequestStatus status;
    private String sourceName;
    private Optional<String> errorMessage;

    public FileRequestInfo(Optional<UUID> id, FileRequestStatus status, String sourceName, Optional<String> errorMessage) {
        this.id = id;
        this.status = status;
        this.sourceName = sourceName;
        this.errorMessage = errorMessage;
    }

    public static FileRequestInfo from(FileRequest request) {
        return new FileRequestInfo(request.getId(), request.getStatus(), request.getSourceName(), request.getErrorMessage());
    }

    public Optional<UUID> getId() {
        return id;
    }

    public void setId(Optional<UUID> id) {
        this.id = id;
    }

    public FileRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FileRequestStatus status) {
        this.status = status;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Optional<String> errorMessage) {
        this.errorMessage = errorMessage;
    }
}
