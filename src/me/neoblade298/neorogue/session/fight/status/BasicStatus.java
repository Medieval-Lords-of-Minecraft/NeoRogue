package me.neoblade298.neorogue.session.fight.status;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(FightData data, int stacks, int seconds) {
		onApply(data, stacks);
		
		if (seconds <= 0) return;
		data.addTask(new BukkitRunnable() { 
			public void run() {
				onApply(data, -stacks);
			}
		}.runTaskLater(NeoRogue.inst(), seconds * 20));
	}
	
	public void onApply(FightData applier, int stacks) {
		this.stacks += stacks;
		slices.add(applier, stacks);
	}
}
