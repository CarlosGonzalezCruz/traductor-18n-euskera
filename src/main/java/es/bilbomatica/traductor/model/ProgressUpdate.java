package es.bilbomatica.traductor.model;

public class ProgressUpdate {

    private int current;

    private int total;

    private boolean done;

    public ProgressUpdate(int current, int total, boolean done) {
        this.current = current;
        this.total = total;
        this.done = done;
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
}
