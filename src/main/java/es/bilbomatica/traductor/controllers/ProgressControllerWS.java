package es.bilbomatica.traductor.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import es.bilbomatica.traductor.model.ErrorMessage;
import es.bilbomatica.traductor.model.ProgressUpdate;
import es.bilbomatica.traductor.model.CancellationToken;

@Controller
public class ProgressControllerWS {

    @Autowired
    private SimpMessagingTemplate messageSender;

    private Optional<CancellationToken> cancellationToken;
   
    @MessageMapping("/progress")
    public void subscribe(@Payload String message) {
        // No hacer nada
    }

    @MessageMapping("/cancel")
    public void requestCancel() {
        cancellationToken.ifPresent(t -> t.setCancellationRequested(true));
    }
    
    public void sendUpdate(ProgressUpdate update) {
        messageSender.convertAndSend("/topic/updates", update);
    }

    public void sendError(ErrorMessage message) {
        messageSender.convertAndSend("/topic/errors", message);
    }

    public void assignCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = Optional.of(cancellationToken);
    }

    public void revokeCancellationToken() {
        this.cancellationToken = Optional.empty();
    }
}
