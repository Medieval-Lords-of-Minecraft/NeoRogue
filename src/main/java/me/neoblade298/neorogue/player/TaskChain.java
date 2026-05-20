package me.neoblade298.neorogue.player;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class TaskChain {
    private FightData data;
    private Task curr;
    private long initDelay;

    public TaskChain(FightData data, long initDelay) {
        this.data = data;
        this.initDelay += initDelay;
    }

    public TaskChain(FightData data, Runnable runnable, long initDelay) {
        this.data = data;
        then(runnable, initDelay);
    }

    public TaskChain(FightData data, Runnable runnable) {
        this.data = data;
        then(runnable, 0);
    }

    public TaskChain then(Runnable runnable) {
        return then(runnable, 0);
    }

    public TaskChain then(Runnable runnable, long delay) {
        if (curr != null) {
            Task prev = curr;
            curr = new Task(data, runnable);
            prev.chain(curr, delay);
        } else {
            curr = new Task(data, runnable);
            final Task task = curr;
            BukkitRunnable br = new BukkitRunnable() {
                public void run() {
                    task.run();
                }
            };
            data.addTask(br.runTaskLater(NeoRogue.inst(), initDelay + delay));
        }
        return this;
    }
}
