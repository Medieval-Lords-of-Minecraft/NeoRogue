package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import net.kyori.adventure.text.Component;

// Grants the mob 20% more maximum health.
public class Beefy extends MobModifier {
	private static final double MULTIPLIER = 1.2;

	public Beefy() {
		super("Beefy", Component.text("Beefy"),
				Component.text("Has 20% more health."), false);
	}

	@Override
	public void initialize(FightData mob) {
		// Delay so this runs after MythicMobs/FightInstance finish setting the mob's scaled health.
		Bukkit.getScheduler().runTaskLater(NeoRogue.inst(), () -> {
			LivingEntity ent = mob.getEntity();
			if (ent == null || !ent.isValid()) return;
			AttributeInstance attr = ent.getAttribute(Attribute.MAX_HEALTH);
			if (attr == null) return;
			double newMax = attr.getBaseValue() * MULTIPLIER;
			attr.setBaseValue(newMax);
			ent.setHealth(newMax);
			mob.updateDisplayName();
		}, 2L);
	}
}
