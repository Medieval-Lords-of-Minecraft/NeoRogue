package me.neoblade298.neorogue.session.fight.modifier;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

// Steadily reduces the mob's ability cooldowns, letting it use skills more often over time.
public class Alacrity extends MobModifier {
	private static final int INTERVAL = 10; // seconds between reductions
	private static final long REDUCTION_TICKS = 20L; // 1 second, in MythicMobs skill cooldown ticks

	// MythicMobs' per-skill cooldown map isn't in the compile-time API, so we resolve it reflectively.
	private static Field cooldownsField;
	private static boolean fieldResolved = false;

	public Alacrity() {
		super("Alacrity", Component.text("Alacrity"),
				Component.text("Reduces its ability cooldowns by 1s every 10s."), false);
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
	@SuppressWarnings("unchecked")
	private static void reduceCooldowns(ActiveMob am) {
		try {
			if (!fieldResolved) {
				cooldownsField = ActiveMob.class.getField("cooldowns");
				fieldResolved = true;
			}
			if (cooldownsField == null) return;
			Map<String, Long> cooldowns = (Map<String, Long>) cooldownsField.get(am);
			if (cooldowns == null) return;
			for (Entry<String, Long> ent : cooldowns.entrySet()) {
				ent.setValue(Math.max(0, ent.getValue() - REDUCTION_TICKS));
			}
		} catch (Exception ex) {
			// Field unavailable in this MythicMobs build; stop retrying every tick.
			fieldResolved = true;
			cooldownsField = null;
		}
	}
}
