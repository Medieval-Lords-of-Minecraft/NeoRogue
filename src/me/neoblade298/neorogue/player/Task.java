package me.neoblade298.neorogue.player;

import java.util.ArrayList;

public class Task {
    private long initDelay;
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    public Task(long initDelay) {
        this.initDelay = initDelay;
    }

    public void then(Runnable runnable) {
        
    }
}
