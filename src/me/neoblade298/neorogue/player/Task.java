package me.neoblade298.neorogue.player;

import java.util.ArrayList;

import org.bukkit.scheduler.BukkitRunnable;

public class Task {
    private int idx = 0;
    private long initDelay;
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    public Task(long initDelay) {
        this.initDelay = initDelay;
    }

    public void then(Runnable runnable) {
        BukkitRunnable br = new BukkitRunnable() {
            int i = idx++;
            public void run() {
                runnable.run();
                if (runnables.size() > i + 1) {
                    runnables.get(i + 1).run();
                }
            }
        };
    }
}
