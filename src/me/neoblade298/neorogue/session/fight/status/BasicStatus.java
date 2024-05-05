package me.neoblade298.neorogue.session.fight.status;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;

public class BasicStatus extends Status {
	public BasicStatus(String id, FightData target) {
		super(id, target);
	}

	@Override
	public void apply(FightData data, int stacks, int ticks) {
		System.out.println("Applying " + id + " " + stacks);
		onApply(data, stacks);
		
		if (ticks <= 0) return;
		data.addTask(new BukkitRunnable() { 
			public void run() {
				System.out.println("Deapplying " + id + " " + stacks);
				onApply(data, -stacks);
			}
		}.runTaskLater(NeoRogue.inst(), ticks * 20));
	}
	
	public void onApply(FightData applier, int stacks) {
		this.stacks += stacks;
		slices.add(applier, stacks);
	}
}
