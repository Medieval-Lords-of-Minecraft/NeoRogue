package me.neoblade298.neorogue.session.fight.status;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		onApply(applier, stacks);
		
		if (seconds <= 0) return;
		data.addTask(new BukkitRunnable() { 
			public void run() {
				onApply(applier, -stacks);
			}
		}.runTaskLater(NeoRogue.inst(), seconds * 20));
	}
	
	public void onApply(UUID applier, int stacks) {
		this.stacks += stacks;
		slices.add(applier, stacks);
	}
}
