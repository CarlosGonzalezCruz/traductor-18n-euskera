package es.bilbomatica.traductor.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import es.bilbomatica.traductor.model.FileRequestInfo;
import es.bilbomatica.traductor.model.ProgressUpdate;

@Controller
public class ProgressControllerWS {

    @Autowired
    private SimpMessagingTemplate messageSender;

   
    @MessageMapping("/progress")
    public void subscribe(@Payload String message) {
        // No hacer nada
    }
    
    public void sendUpdate(ProgressUpdate update) {
        messageSender.convertAndSend("/topic/updates", update);
    }

    public void sendRequestList(List<FileRequestInfo> requestList) {
        messageSender.convertAndSend("/topic/requests", requestList);
    }
}
