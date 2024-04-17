package es.bilbomatica.traductor.model;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
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
    private byte[] sourceFileBytes;
    private i18nResourceFile fileData;
    private FileRequestStatus status;
    private Optional<String> errorMessage;

    private FileRequest(String sourceName, byte[] sourceFileBytes, i18nResourceFile fileData) {
        this.id = Optional.empty();
        this.sourceName = sourceName;
        this.sourceFileBytes = sourceFileBytes;
        this.fileData = fileData;
        this.status = FileRequestStatus.PENDING;
        this.errorMessage = Optional.empty();
    }

    public static FileRequest create(@NonNull String filetype, @NonNull MultipartFile file) throws InvalidI18nResourceTypeException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, WrongFormatException {
        
        i18nResourceFile fileData;
        String filename = file.getOriginalFilename();
        
        if(I18nResourceFileType.AUTO.matches(filetype) && filename != null) {
            if(filename.endsWith(".properties")) {
                fileData = i18nPropertiesResourceFile.load(file);
            } else if(filename.endsWith(".json")) {
                fileData = i18nJsonResourceFile.load(file);
            } else if(filename.endsWith(".xml")) {
                fileData = i18nXMLResourceFile.load(file);
            } else {
                throw new InvalidI18nResourceTypeException(filename);
            }
        } else if(I18nResourceFileType.PROPERTIES.matches(filetype)) {
            fileData = i18nPropertiesResourceFile.load(file);
        } else if(I18nResourceFileType.JSON.matches(filetype)) {
            fileData = i18nJsonResourceFile.load(file);
        } else if(I18nResourceFileType.XML.matches(filetype)) {
            fileData = i18nXMLResourceFile.load(file);
        } else {
            throw new InvalidI18nResourceTypeException(filetype);
        }

        return new FileRequest(file.getOriginalFilename(), file.getBytes(), fileData);
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


    public byte[] getOriginalFileBytes() {
        return this.sourceFileBytes;
    }


    public i18nResourceFile getResourceFile() {
        return this.fileData;
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
}
