package es.bilbomatica.traductor.model;

import java.util.Optional;

public class ProgressUpdate {

    private int current;

    private int total;

    private boolean done;

    private Optional<Long> remainingTimeNS;

    public ProgressUpdate(int current, int total, boolean done, Optional<Long> remainingTimeNS) {
        this.current = current;
        this.total = total;
        this.done = done;
        this.remainingTimeNS = remainingTimeNS;
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
