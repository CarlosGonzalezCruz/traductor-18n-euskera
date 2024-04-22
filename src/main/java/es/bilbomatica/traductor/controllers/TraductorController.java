package es.bilbomatica.traductor.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import es.bilbomatica.test.logic.FileRequestStatus;
import es.bilbomatica.test.logic.I18nResourceFileType;
import es.bilbomatica.traductor.DirectorService;
import es.bilbomatica.traductor.FileRequestQueueService;
import es.bilbomatica.traductor.exceptions.BusinessException;
import es.bilbomatica.traductor.exceptions.FileRequestNotReadyException;
import es.bilbomatica.traductor.model.ErrorMessage;
import es.bilbomatica.traductor.model.FileRequest;
import es.bilbomatica.traductor.model.FileRequestWSInfo;
import es.bilbomatica.traductor.persistence.FileLocation;
import es.bilbomatica.traductor.persistence.FileManagerService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TraductorController {

    @Autowired
    private DirectorService directorService;

    @Autowired
    private FileRequestQueueService fileRequestQueueService;

    @Autowired
    private FileManagerService fileManagerService;


    @GetMapping("/")
    public String index() {
        return "index.html";
    }


    @ResponseBody
    @PostMapping("/request")
	public UUID addFile(HttpServletResponse response,
            @RequestParam Optional<String> filetype,
            @RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException, BusinessException {

        UUID sourceFileId = fileManagerService.save(file.getOriginalFilename(), FileLocation.ORIGINAL, o -> file.getInputStream().transferTo(o));
        String filename = file.getOriginalFilename();
        InputStream fileStream = file.getInputStream();
        FileRequest request = FileRequest.create(filetype.orElse(I18nResourceFileType.AUTO.getName()), filename, fileStream, sourceFileId);
        UUID fileRequestId = fileRequestQueueService.add(request);
        directorService.resume();
        
        return fileRequestId;
	}


    @ResponseBody
    @GetMapping("/request")
    public FileRequest getRequest(@RequestParam UUID requestId) {
        return fileRequestQueueService.get(requestId);
    }


    @ResponseBody
    @GetMapping("/request/all")
    public List<FileRequestWSInfo> getAllRequestsInfo() {
        return fileRequestQueueService.getAllRequestsInfo();
    }


    @GetMapping("/request/source")
    public void downloadOriginal(HttpServletResponse response, @RequestParam UUID requestId) throws IOException {
        response.setContentType("text/plain");

        FileRequest fileRequest = fileRequestQueueService.get(requestId);
        
        response.addHeader("Content-Disposition", "attachment; filename="
        + fileRequestQueueService.get(requestId).getSourceName());

        fileManagerService.load(fileRequest.getSourceFileId()).transferTo(response.getOutputStream());
        response.flushBuffer();
    }


    @GetMapping("/request/translated")
    public void downloadTranslated(HttpServletResponse response, @RequestParam UUID requestId) throws IOException, FileRequestNotReadyException {
        response.setContentType("text/plain");

        FileRequest fileRequest = fileRequestQueueService.get(requestId);
        if(!FileRequestStatus.DONE.equals(fileRequest.getStatus())) {
            throw new FileRequestNotReadyException(fileRequest.getSourceName());
        }
        
        response.addHeader("Content-Disposition", "attachment; filename="
        + fileRequestQueueService.get(requestId).getTranslatedName());

        fileManagerService.load(fileRequest.getTranslatedFileId().orElseThrow(
            () -> new FileRequestNotReadyException(fileRequest.getSourceName()))
        ).transferTo(response.getOutputStream());
        response.flushBuffer();
    }


    @DeleteMapping("/request")
    public void removeFileRequest(HttpServletResponse response, @RequestParam UUID requestId) throws IOException {
        FileRequest fileRequest = fileRequestQueueService.get(requestId);
        fileRequest.setStatus(FileRequestStatus.CANCELLED);
        fileManagerService.delete(fileRequest.getSourceFileId());
        if(fileRequest.getTranslatedFileId().isPresent()) {
            fileManagerService.delete(fileRequest.getTranslatedFileId().get());
        }

        fileRequestQueueService.remove(requestId);
        directorService.resume();
        response.setStatus(200);
    }


    @DeleteMapping("/request/completed")
    public void removeCompletedRequests(HttpServletResponse response) throws IOException {
        List<FileRequest> deletedFileRequests = fileRequestQueueService.removeCompleted();

        for(FileRequest request : deletedFileRequests) {
            fileManagerService.delete(request.getSourceFileId());
            if(request.getTranslatedFileId().isPresent()) {
                fileManagerService.delete(request.getTranslatedFileId().get());
            }
        }

        directorService.resume();
        response.setStatus(200);
    }


    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        if(e instanceof BusinessException) {
            BusinessException eAsBEx = (BusinessException) e;
            return ResponseEntity.badRequest().body(new ErrorMessage(eAsBEx.getUserMessage()));
            
        } else {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorMessage("Ha ocurrido un problema inesperado con el servidor."));
        }
    }

}
