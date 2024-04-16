package es.bilbomatica.traductor.model;

import java.util.Optional;
import java.util.UUID;

import es.bilbomatica.test.logic.FileRequestStatus;

public class ProgressUpdate {

    private UUID requestId;
    private FileRequestStatus requestStatus;
    private int current;
    private int total;
    private boolean done;
    private Optional<Long> remainingTimeNS;

    public ProgressUpdate(UUID requestId, FileRequestStatus requestStatus, int current, int total, boolean done, Optional<Long> remainingTimeNS) {
        this.current = current;
        this.requestStatus = requestStatus;
        this.total = total;
        this.done = done;
        this.remainingTimeNS = remainingTimeNS;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID id) {
        this.requestId = id;
    }

    public FileRequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(FileRequestStatus status) {
        this.requestStatus = status;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Optional<Long> getRemainingTimeNS() {
        return remainingTimeNS;
    }

    public void setRemainingTimeNS(Optional<Long> remainingTimeNS) {
        this.remainingTimeNS = remainingTimeNS;
    }
}
