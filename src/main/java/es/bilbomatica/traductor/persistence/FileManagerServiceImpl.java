package es.bilbomatica.traductor.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.function.ThrowingConsumer;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import es.bilbomatica.test.logic.I18nResourceFileType;
import es.bilbomatica.traductor.exceptions.InvalidI18nResourceTypeException;
import es.bilbomatica.traductor.exceptions.WrongFormatException;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestPersistentInfo;

@Service
public class FileManagerServiceImpl implements FileManagerService {

    private final String FILENAME_MATCH_REGEX = "^(?<basename>.*?)(?:\\((?<number>\\d+)\\))?(?<extension>\\..*)?$";
    private final String INDEX_FILENAME = "request-index.json";
    
    private Map<UUID, Path> storedFiles;
    private Pattern filenameMatchPattern;

    @Value("${files.location.original}")
    private String originalPath;

    @Value("${files.location.translated}")
    private String translatedPath;

    @Value("${index.location}")
    private String indexPath;

    private FileManagerServiceImpl() {
        this.storedFiles = new ConcurrentHashMap<>();
        this.filenameMatchPattern = Pattern.compile(FILENAME_MATCH_REGEX);
    }

    @Override
    public UUID save(String filename, FileLocation location, ThrowingConsumer<OutputStream> dataProvider) throws IOException {
        UUID fileId = UUID.randomUUID();
        Path parentDirectory = getPath(location);

        if(!Files.exists(parentDirectory)) {
            Files.createDirectories(parentDirectory);
        }

        synchronized(this) {
            String availableFilename = getNextAvailableFilename(parentDirectory, filename);
            Path filePath = Paths.get(parentDirectory.toString(), availableFilename);
            Files.createFile(filePath);
    
            OutputStream fileStream = Files.newOutputStream(filePath);
            dataProvider.accept(fileStream);
            fileStream.close();
    
            storedFiles.put(fileId, filePath);
        }

        return fileId;
    }

    @Override
    public boolean exists(UUID fileId) {
        synchronized(this) {
            Optional<Path> filePath = Optional.ofNullable(this.storedFiles.get(fileId));
            return filePath.map(Files::exists).orElse(false);
        }
    }

    @Override
    public InputStream load(UUID fileId) throws IOException {
        synchronized(this) {
            Path filePath = this.storedFiles.get(fileId);
            return Files.newInputStream(filePath);
        }
    }

    @Override
    public void delete(UUID fileId) throws IOException {
        synchronized(this) {
            Path path = this.storedFiles.get(fileId);
            Files.delete(path);
        }
    }

    @Override
    public void updateIndex(List<FileRequest> fileRequests) throws IOException {
        
        createFileWithDirectoriesIfDoesntExist(Paths.get(indexPath), INDEX_FILENAME);
        Path indexPath = Paths.get(this.indexPath, INDEX_FILENAME);
        List<FileRequestPersistentInfo> persistentInfo = new ArrayList<>();

        synchronized(this) {
            for(FileRequest request : fileRequests) {
                String sourceFilePath = storedFiles.get(request.getSourceFileId()).toString();
                Optional<String> translatedFilePath = request.getTranslatedFileId().map(storedFiles::get).map(Object::toString);
                persistentInfo.add(FileRequestPersistentInfo.create(request, sourceFilePath, translatedFilePath));
            }
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            OutputStream fileOutputStream = Files.newOutputStream(indexPath);
            mapper.writeValue(fileOutputStream, persistentInfo);
            fileOutputStream.close();
        }

    }

    @Override
    public List<FileRequest> loadIndex() throws IOException {

        boolean newlyCreated = createFileWithDirectoriesIfDoesntExist(Paths.get(indexPath), INDEX_FILENAME);
        if(newlyCreated) {
            return new ArrayList<>();
        }
        
        Path indexPath = Paths.get(this.indexPath, INDEX_FILENAME);
        List<FileRequest> fileRequests = new ArrayList<>();
        List<FileRequestPersistentInfo> persistentInfo;
        
        synchronized(this) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            InputStream fileInputStream = Files.newInputStream(indexPath);
            persistentInfo = mapper.readValue(fileInputStream, new TypeReference<>() {});
            fileInputStream.close();
        }

        for(FileRequestPersistentInfo info : persistentInfo) {
            
            UUID sourceFileId = UUID.randomUUID();
            Optional<UUID> translatedFileId = Optional.empty();

            storedFiles.put(sourceFileId, Paths.get(info.getOriginalFilePath()));
            if(info.getTranslatedFilePath().isPresent()) {
                translatedFileId = Optional.of(UUID.randomUUID());
                storedFiles.put(translatedFileId.get(), info.getTranslatedFilePath().map(Paths::get).get());
            }

            try {
                InputStream sourceFile = Files.newInputStream(Paths.get(info.getOriginalFilePath()));
                FileRequest fileRequest = FileRequest.restore(info, I18nResourceFileType.AUTO.getName(), sourceFile, sourceFileId, translatedFileId);
                fileRequests.add(fileRequest);
                sourceFile.close();

            } catch (InvalidI18nResourceTypeException | WrongFormatException | XPathExpressionException
                    | IOException | ParserConfigurationException | SAXException e) {
                System.err.println("No se ha podido cargar el archivo \"" + info.getOriginalFilePath() + "\". Motivo: " + e.getLocalizedMessage());
            }
        }

        return fileRequests;
    }


    @Override
    public void clearOrphans() throws IOException {
        Path[] directoriesToClear = new Path[] { Paths.get(originalPath), Paths.get(translatedPath) };
        Set<Path> filesToKeep = new HashSet<>(storedFiles.values());
        
        for(Path directory : directoriesToClear) {
            
            if(!Files.exists(directory)) {
                continue;
            }

            Files.list(directory)
                .filter(f -> !filesToKeep.contains(f))
                .forEach(f -> {
                    try {
                        Files.delete(f);
                    } catch(IOException e) {
                        System.err.println("Error al eliminar archivo: " + e);
                    }
                });

        }
    }


    private Path getPath(FileLocation location) {
        Path ret;

        switch(location) {
            case ORIGINAL:
                ret = Paths.get(originalPath);
                break;
            case TRANSLATED:
                ret = Paths.get(translatedPath);
                break;
            default:
                throw new IllegalArgumentException("Ubicación " + location + " no reconocida.");
        }

        return ret;
    }


    private boolean createFileWithDirectoriesIfDoesntExist(Path directory, String filename) throws IOException {
        boolean newlyCreated = false;

        if(!Files.exists(directory)) {
            Files.createDirectories(directory);
            newlyCreated = true;
        }

        Path filePath = Paths.get(directory.toString(), filename);

        if(!Files.exists(filePath)) {
            Files.createFile(filePath);
            newlyCreated = true;
        }

        return newlyCreated;
    }


    private String getNextAvailableFilename(Path directory, String requestedFilename) {
        String currentFilename = requestedFilename;
        Matcher matcher;

        while(Files.exists(Paths.get(directory.toString(), currentFilename))) {
            matcher = filenameMatchPattern.matcher(currentFilename);
            
            if(!matcher.matches()) {
                throw new IllegalArgumentException("El formato del nombre de archivo \"" + requestedFilename + "\" no es el que se esperaba.");
            }

            String basename = matcher.group("basename");
            String extension = Optional.ofNullable(matcher.group("extension")).orElse("");
            Optional<String> number = Optional.ofNullable(matcher.group("number"));

            if(number.isPresent()) {
                Integer numberValue = Integer.valueOf(number.get());
                numberValue += 1;
                number = Optional.of(numberValue.toString());
            } else {
                number = Optional.of("1");
            }

            currentFilename = basename + "(" + number.get() + ")" + extension;
        }
        
        return currentFilename;
    }


}
