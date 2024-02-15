package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

import java.util.UUID;

public class PoisonStatus extends DurationStatus {
	private static String id = "POISON";

	public PoisonStatus(FightData data) {
		super(id, data);
	}

	@Override
	public void apply(UUID applier, int stacks, int seconds) {
		if (data instanceof PlayerFightData) {
			data.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.POISON, seconds * 20, 0));
		}
		super.apply(applier, stacks, seconds);
	}
	
	@Override
	public void onTickAction() {
		// Owner is arbitrarily first slice of damage
		FightData owner = FightInstance.getFightData(slices.getSliceOwners().entrySet().iterator().next().getKey());
		DamageMeta meta = new DamageMeta(owner);
		meta.isSecondary(true);
		for (Entry<UUID, Integer> ent : slices.getSliceOwners().entrySet()) {
			meta.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.POISON, true));
		}
		FightInstance.dealDamage(meta, data.getEntity());
	}
}
