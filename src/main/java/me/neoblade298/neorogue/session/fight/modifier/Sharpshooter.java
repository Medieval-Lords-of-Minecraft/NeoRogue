package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

// Every few seconds, the mob casts its "Sharpshooter" MythicMobs skill.
public class Sharpshooter extends MobModifier {
	private static final int INTERVAL = 5; // seconds

	public Sharpshooter() {
		super("Sharpshooter", Component.text("Sharpshooter"),
				Component.text("Fires an arrow at a random enemy every " + INTERVAL + "s."), false);
	}

	@Override
	public void initialize(FightData mob) {
		mob.addTickAction(new TickAction() {
			private int counter = 0;

			@Override
			public TickResult run() {
				LivingEntity ent = mob.getEntity();
				if (ent == null || !ent.isValid()) return TickResult.REMOVE;
				if (++counter < INTERVAL) return TickResult.KEEP;
				counter = 0;
				NeoRogue.mythicApi.castSkill(ent, getId());
				return TickResult.KEEP;
			}
		});
	}
}
