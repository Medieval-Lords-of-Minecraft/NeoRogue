package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

// Steadily reduces the mob's ability cooldowns, letting it use skills more often over time.
public class Alacrity extends MobModifier {
	private static final int INTERVAL = 10; // seconds between reductions
	private static final int REDUCTION_TICKS = 20; // 1 second, in MythicMobs skill cooldown ticks

	public Alacrity() {
		super("Alacrity", Component.text("Alacrity"),
				Component.text("Reduces its ability cooldowns by 1s every 10s."),
				Scope.BOSS_ONLY);
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

				ActiveMob am = mob.getActiveMob();
				if (am != null) reduceCooldowns(am);
				return TickResult.KEEP;
			}
		});
	}

	// MythicMobs stores skill cooldowns as the tick at which each skill is next usable; lowering
	// those values makes the skills available sooner.
	private static void reduceCooldowns(ActiveMob am) {
		int gcd = am.getGlobalCooldown();
		if (gcd > 0) am.setGlobalCooldown(Math.max(0, gcd - REDUCTION_TICKS));
	}
}
