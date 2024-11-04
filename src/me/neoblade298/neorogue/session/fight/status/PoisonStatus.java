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

public class PoisonStatus extends DurationStatus {
	private static String id = "POISON";

	public PoisonStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}

	@Override
	public void apply(FightData applier, int stacks, int ticks) {
		if (holder instanceof PlayerFightData) {
			holder.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.POISON, ticks, 0));
		}
		super.apply(applier, stacks, ticks);
	}
	
	@Override
	public void onTickAction() {
		// Owner is arbitrarily first slice of damage
		FightData owner = slices.getSliceOwners().entrySet().iterator().next().getKey();
		DamageMeta meta = new DamageMeta(owner);
		meta.isSecondary(true);
		for (Entry<FightData, Integer> ent : slices.getSliceOwners().entrySet()) {
			meta.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.POISON, true));
		}
		FightInstance.dealDamage(meta, holder.getEntity());
	}
}
