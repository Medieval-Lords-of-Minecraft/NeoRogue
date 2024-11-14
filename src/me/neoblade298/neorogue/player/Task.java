package me.neoblade298.neorogue.player;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class Task {
    FightData data;
    private Runnable runnable;
    private Task next;
    private long delay, period = -1;

    public Task(FightData data, Runnable runnable) {
        this.data = data;
        this.runnable = runnable;
    }

    public void run() {
        runnable.run();
        if (next != null) {
            scheduleNext();
        }
    }

    public void chain(Task next, long delay) {
        this.next = next;
        this.delay = delay;
    }

    public void chain(Task next, long delay, long period) {
        this.next = next;
        this.delay = delay;
        this.period = period;
    }

    private void scheduleNext() {
        BukkitRunnable rn = new BukkitRunnable() {
            public void run() {
                next.run();
            }
        };
        if (period == -1) {
            data.addTask(rn.runTaskLater(NeoRogue.inst(), delay));
        }
        else {
            data.addTask(rn.runTaskTimer(NeoRogue.inst(), delay, period));
        }
    }
}
