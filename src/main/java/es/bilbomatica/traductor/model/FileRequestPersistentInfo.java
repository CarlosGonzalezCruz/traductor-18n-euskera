package es.bilbomatica.traductor.model;

import java.util.Optional;

import es.bilbomatica.test.logic.FileRequestStatus;

public class FileRequestPersistentInfo {

    private String sourceFilename;
    private String translatedFilename;
    private FileRequestStatus status;
    private String originalFilePath;
    private Optional<String> translatedFilePath;
    private Optional<String> errorMessage;


    private FileRequestPersistentInfo() {}

    private FileRequestPersistentInfo(String sourceFilename, String translatedFilename, FileRequestStatus status, String originalFilePath, Optional<String> translatedFilePath, Optional<String> errorMessage) {
        this.sourceFilename = sourceFilename;
        this.translatedFilename = translatedFilename;
        this.status = status;
        this.originalFilePath = originalFilePath;
        this.translatedFilePath = translatedFilePath;
        this.errorMessage = errorMessage;
    }

    public static FileRequestPersistentInfo create(FileRequest fileRequest, String originalFilePath, Optional<String> translatedFilePath) {
        return new FileRequestPersistentInfo(fileRequest.getSourceName(), fileRequest.getTranslatedName(), fileRequest.getStatus(), originalFilePath, translatedFilePath, fileRequest.getErrorMessage());
    }


    public String getSourceFilename() {
        return sourceFilename;
    }


    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }


    public String getTranslatedFilename() {
        return translatedFilename;
    }


    public void setTranslatedFilename(String translatedFilename) {
        this.translatedFilename = translatedFilename;
    }


    public FileRequestStatus getStatus() {
        return status;
    }


    public void setStatus(FileRequestStatus status) {
        this.status = status;
    }


    public String getOriginalFilePath() {
        return originalFilePath;
    }


    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }


    public Optional<String> getTranslatedFilePath() {
        return translatedFilePath;
    }


    public void setTranslatedFilePath(Optional<String> translatedFilePath) {
        this.translatedFilePath = translatedFilePath;
    }


    public Optional<String> getErrorMessage() {
        return errorMessage;
    }


    public void setErrorMessage(Optional<String> errorMessage) {
        this.errorMessage = errorMessage;
    }

}
