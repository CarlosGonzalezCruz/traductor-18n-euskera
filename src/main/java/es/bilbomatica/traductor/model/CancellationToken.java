package es.bilbomatica.traductor.model;

public class CancellationToken {

    private boolean cancellationRequested;

    public CancellationToken() {
        this.cancellationRequested = false;
    }

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public void setCancellationRequested(boolean cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
    }
}

