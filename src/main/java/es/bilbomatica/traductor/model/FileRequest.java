package es.bilbomatica.traductor.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.lang.NonNull;
import org.xml.sax.SAXException;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.test.logic.I18nResourceFileType;
import es.bilbomatica.test.logic.i18nJsonResourceFile;
import es.bilbomatica.test.logic.i18nPropertiesResourceFile;
import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.test.logic.i18nXMLResourceFile;
import es.bilbomatica.traductor.exceptions.InvalidI18nResourceTypeException;
import es.bilbomatica.traductor.exceptions.WrongFormatException;

public class FileRequest {

    private Optional<UUID> id;
    private String sourceName;
    private String translatedName;
    private UUID sourceFileId;
    private Optional<UUID> translatedFileId;
    private Optional<i18nResourceFile> fileData;
    private FileRequestStatus status;
    private Optional<String> errorMessage;
    private Optional<ProgressUpdate> lastProgressUpdate;

    private FileRequest(String sourceName, String translatedName, UUID sourceFileId, i18nResourceFile fileData) {
        this.id = Optional.empty();
        this.sourceName = sourceName;
        this.translatedName = translatedName;
        this.sourceFileId = sourceFileId;
        this.translatedFileId = Optional.empty();
        this.fileData = Optional.of(fileData);
        this.status = FileRequestStatus.PENDING;
        this.errorMessage = Optional.empty();
        this.lastProgressUpdate = Optional.empty();
    }

    private FileRequest(String sourceName, String translatedName, UUID sourceFileId, Optional<UUID> translatedFileId, Optional<i18nResourceFile> fileData, FileRequestStatus status, Optional<String> errorMessage) {
        this.id = Optional.empty();
        this.sourceName = sourceName;
        this.translatedName = translatedName;
        this.sourceFileId = sourceFileId;
        this.translatedFileId = translatedFileId;
        this.fileData = fileData;
        this.status = status;
        this.errorMessage = errorMessage;
        this.lastProgressUpdate = Optional.empty();
    }


    public static FileRequest create(String filetype, String sourceName, InputStream file, UUID sourceFileId) throws InvalidI18nResourceTypeException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, WrongFormatException {
        i18nResourceFile fileData = parseFileData(filetype, sourceName, file);
        return new FileRequest(sourceName, fileData.getTranslatedName(), sourceFileId, fileData);
    }


    public static FileRequest restore(FileRequestPersistentInfo persistentInfo, String filetype, InputStream file, UUID sourceFileId, Optional<UUID> translatedFileId) throws InvalidI18nResourceTypeException, WrongFormatException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        Optional<i18nResourceFile> fileData = Optional.empty();
        if(FileRequestStatus.PENDING.equals(persistentInfo.getStatus()) || FileRequestStatus.IN_PROGRESS.equals(persistentInfo.getStatus())) {
            fileData = Optional.of(parseFileData(filetype, persistentInfo.getSourceFilename(), file));
        }
        return new FileRequest(persistentInfo.getSourceFilename(), persistentInfo.getTranslatedFilename(), sourceFileId, translatedFileId, fileData, persistentInfo.getStatus(), persistentInfo.getErrorMessage());
    }


    private static i18nResourceFile parseFileData(String filetype, String sourceName, InputStream file) throws InvalidI18nResourceTypeException, IOException, WrongFormatException, XPathExpressionException, ParserConfigurationException, SAXException {
        i18nResourceFile fileData;
        
        if(I18nResourceFileType.AUTO.matches(filetype)) {
            if(sourceName.endsWith(".properties")) {
                fileData = i18nPropertiesResourceFile.load(sourceName, file);
            } else if(sourceName.endsWith(".json")) {
                fileData = i18nJsonResourceFile.load(sourceName, file);
            } else if(sourceName.endsWith(".xml")) {
                fileData = i18nXMLResourceFile.load(sourceName, file);
            } else {
                throw new InvalidI18nResourceTypeException(sourceName);
            }
        } else if(I18nResourceFileType.PROPERTIES.matches(filetype)) {
            fileData = i18nPropertiesResourceFile.load(sourceName, file);
        } else if(I18nResourceFileType.JSON.matches(filetype)) {
            fileData = i18nJsonResourceFile.load(sourceName, file);
        } else if(I18nResourceFileType.XML.matches(filetype)) {
            fileData = i18nXMLResourceFile.load(sourceName, file);
        } else {
            throw new InvalidI18nResourceTypeException(filetype);
        }

        return fileData;
    } 


    public Optional<UUID> getId() {
        return this.id;
    }


    public void setId (@NonNull Optional<UUID> id) {
        this.id = id;
    }


    public String getSourceName() {
        return this.sourceName;
    }


    public String getTranslatedName() {
        return this.translatedName;
    }


    public UUID getSourceFileId() {
        return this.sourceFileId;
    }


    public Optional<UUID> getTranslatedFileId() {
        return this.translatedFileId;
    }


    public void setTranslatedFileId(Optional<UUID> translatedFileId) {
        this.translatedFileId = translatedFileId;
    }


    public i18nResourceFile getFileData() {
        return this.fileData.orElseThrow(
            () -> new IllegalStateException("Se ha intentado acceder a los datos de " + getSourceName() + " después de su liberación.")
        );
    }


    public void releaseFileData() {
        this.fileData = Optional.empty();
    }


    public FileRequestStatus getStatus() {
        return this.status;
    }


    public void setStatus(@NonNull FileRequestStatus status) {
        this.status = status;
    }


    public Optional<String> getErrorMessage() {
        return this.errorMessage;
    }


    public void setErrorMessage(Optional<String> errorMessage) {
        this.errorMessage = errorMessage;
    }


    public Optional<ProgressUpdate> getLastProgressUpdate() {
        return this.lastProgressUpdate;
    }


    public void setLastProgressUpdate(Optional<ProgressUpdate> progressUpdate) {
        this.lastProgressUpdate = progressUpdate;
    }
}
