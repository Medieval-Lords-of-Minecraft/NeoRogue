package me.neoblade298.neorogue.session.fight.status;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target, StatusClass sc) {
		super(id, target, sc);
	}
	
	public BasicStatus(String id, FightData target, StatusClass sc, boolean hidden) {
		super(id, target, sc, hidden);
	}

	@Override
	public void apply(FightData data, int stacks, int ticks) {
		onApply(data, stacks);
		
		if (ticks <= 0) return;
		data.addTask(new BukkitRunnable() { 
			public void run() {
				onApply(data, -stacks);
			}
		}.runTaskLater(NeoRogue.inst(), ticks));
	}
	
	public void onApply(FightData applier, int stacks) {
		this.stacks += stacks;
		slices.add(applier, stacks);
		if (stacks < 0) {
			stacks = 0;
		}
	}
}
