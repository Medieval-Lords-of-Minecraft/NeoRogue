package me.neoblade298.neorogue.player;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.session.fight.FightData;

public class TaskChain {
    private long currentTick = 0;
    private FightData data;

    public TaskChain(FightData data) {
        this.data = data;
    }

    public TaskChain then(long delay, BukkitRunnable runnable) {
        currentTick += delay;
        return this;
    }
}
