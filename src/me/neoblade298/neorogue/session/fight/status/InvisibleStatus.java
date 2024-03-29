package me.neoblade298.neorogue.session.fight.status;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.session.fight.FightData;

public class InvisibleStatus extends DurationStatus {
	private static String id = "INVISIBLE";

	public InvisibleStatus(FightData data) {
		super(id, data);
	}

	@Override
	public void apply(FightData applier, int stacks, int ticks) {
		data.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks, 0));
		super.apply(applier, stacks, ticks);
	}
}
