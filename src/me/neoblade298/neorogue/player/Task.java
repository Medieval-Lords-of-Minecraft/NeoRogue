package me.neoblade298.neorogue.player;

public class Task {
    private long delay;
    private Runnable task;
    public Task(long delay, Runnable task) {
        this.delay = delay;
        this.task = task;
    }

    private void initialize() {

    }
}
