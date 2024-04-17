package es.bilbomatica.traductor.controllers;

import java.io.IOException;
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
import es.bilbomatica.traductor.model.FileRequestInfo;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TraductorController {

    @Autowired
    private DirectorService directorService;

    @Autowired
    private FileRequestQueueService fileRequestQueueService;


    @GetMapping("/")
    public String index() {
        return "index.html";
    }


    @PostMapping("/request")
	public void translateFile(HttpServletResponse response,
            @RequestParam Optional<String> filetype,
            @RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException, BusinessException {

        FileRequest request = FileRequest.create(filetype.orElse(I18nResourceFileType.AUTO.getName()), file);
        fileRequestQueueService.add(request);
        directorService.resume();
	}


    @ResponseBody
    @GetMapping("/request")
    public FileRequest getRequest(@RequestParam UUID requestId) {
        return fileRequestQueueService.get(requestId);
    }


    @ResponseBody
    @GetMapping("/request/all")
    public List<FileRequestInfo> getAllRequestsInfo() {
        return fileRequestQueueService.getAllRequestsInfo();
    }


    @GetMapping("/request/source")
    public void downloadOriginal(HttpServletResponse response, @RequestParam UUID requestId) throws IOException {
        response.setContentType("text/plain");
        
        response.addHeader("Content-Disposition", "attachment; filename="
        + fileRequestQueueService.get(requestId).getSourceName());
        
        fileRequestQueueService.downloadSource(requestId, response.getOutputStream());
        response.flushBuffer();
    }


    @GetMapping("/request/translated")
    public void downloadTranslated(HttpServletResponse response, @RequestParam UUID requestId) throws IOException, FileRequestNotReadyException {
        response.setContentType("text/plain");
        
        response.addHeader("Content-Disposition", "attachment; filename="
        + fileRequestQueueService.get(requestId).getResourceFile().getName());
       
        fileRequestQueueService.downloadTranslated(requestId, response.getOutputStream());
        response.flushBuffer();
    }


    @DeleteMapping("/request")
    public void removeFileRequest(HttpServletResponse response, @RequestParam UUID requestId) {
        fileRequestQueueService.get(requestId).setStatus(FileRequestStatus.CANCELLED);
        fileRequestQueueService.remove(requestId);
        directorService.resume();
        response.setStatus(200);
    }


    @DeleteMapping("/request/completed")
    public void removeCompletedRequests(HttpServletResponse response) {
        fileRequestQueueService.removeCompleted();
        directorService.resume();
        response.setStatus(200);
    }


    @PostMapping("/request/cancel")
    public void cancelFileRequest(HttpServletResponse response, @RequestParam UUID requestId) {
        fileRequestQueueService.get(requestId).setStatus(FileRequestStatus.CANCELLED);
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
