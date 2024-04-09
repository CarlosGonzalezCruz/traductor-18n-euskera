package es.bilbomatica.traductor.controllers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import es.bilbomatica.test.logic.I18nResourceFileType;
import es.bilbomatica.test.logic.i18nJsonResourceFile;
import es.bilbomatica.test.logic.i18nPropertiesResourceFile;
import es.bilbomatica.test.logic.i18nResourceFile;
import es.bilbomatica.test.logic.i18nXMLResourceFile;
import es.bilbomatica.traductor.TraductorService;
import es.bilbomatica.traductor.exceptions.BusinessException;
import es.bilbomatica.traductor.exceptions.InvalidI18nResourceTypeException;
import es.bilbomatica.traductor.exceptions.WrongFormatException;
import es.bilbomatica.traductor.model.ErrorMessage;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TraductorController {

    @Autowired
    private TraductorService traductorService;

    @Autowired
    private ProgressControllerWS progressControllerWS;

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @PostMapping("/")
	public void translateFile(HttpServletResponse response,
            @RequestParam("filetype") String filetype,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException, BusinessException {

        i18nResourceFile resourceFile = parseResourceFile(filetype, file);
            
        traductorService.translateFile(resourceFile);

        response.setContentType("text/plain");
        response.addHeader("Content-Disposition", "attachment; filename=" + resourceFile.getName());
        resourceFile.writeToOutput(response.getOutputStream());
        response.flushBuffer();
	}


    private i18nResourceFile parseResourceFile(String filetype, MultipartFile file) throws InvalidI18nResourceTypeException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, WrongFormatException {
        
        i18nResourceFile ret;
        String filename = file.getOriginalFilename();
        
        if(I18nResourceFileType.AUTO.matches(filetype) && filename != null) {
            if(filename.endsWith(".properties")) {
                ret = i18nPropertiesResourceFile.load(file);
            } else if(filename.endsWith(".json")) {
                ret = i18nJsonResourceFile.load(file);
            } else if(filename.endsWith(".xml")) {
                ret = i18nXMLResourceFile.load(file);
            } else {
                throw new InvalidI18nResourceTypeException(filename);
            }
        } else if(I18nResourceFileType.PROPERTIES.matches(filetype)) {
            ret = i18nPropertiesResourceFile.load(file);
        } else if(I18nResourceFileType.JSON.matches(filetype)) {
            ret = i18nJsonResourceFile.load(file);
        } else if(I18nResourceFileType.XML.matches(filetype)) {
            ret = i18nXMLResourceFile.load(file);
        } else {
            throw new InvalidI18nResourceTypeException(filetype);
        }

        return ret;
    }

    @ExceptionHandler
    public void handleException(Exception e) {
        if(e instanceof BusinessException) {
            BusinessException eAsBEx = (BusinessException) e;
            progressControllerWS.sendError(new ErrorMessage(eAsBEx.getUserMessage()));
        } else {
            e.printStackTrace();
            progressControllerWS.sendError(new ErrorMessage("Ha ocurrido un problema inesperado en el servidor."));
        }
    }

}
