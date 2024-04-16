package es.bilbomatica.traductor.controllers;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.annotation.Autowired;
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
import es.bilbomatica.traductor.model.FileRequest;
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
    public void removeFileRequest(@RequestParam UUID requestId) {
        fileRequestQueueService.remove(requestId);
        directorService.resume();
    }


    @DeleteMapping("/request/completed")
    public void removeCompletedRequests() {
        fileRequestQueueService.removeCompleted();
        directorService.resume();
    }


    @PostMapping("/request/cancel")
    public void cancelFileRequest(@RequestParam UUID requestId) {
        fileRequestQueueService.get(requestId).setStatus(FileRequestStatus.CANCELLED);
        directorService.resume();
    }


    @ExceptionHandler
    public void handleException(Exception e) {
        if(e instanceof BusinessException) {
            // BusinessException eAsBEx = (BusinessException) e;
            // progressControllerWS.sendError(new ErrorMessage(eAsBEx.getUserMessage()));
        } else {
            e.printStackTrace();
            // progressControllerWS.sendError(new ErrorMessage("Ha ocurrido un problema inesperado en el servidor."));
        }
    }

}
