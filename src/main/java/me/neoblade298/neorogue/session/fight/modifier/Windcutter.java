package me.neoblade298.neorogue.session.fight.modifier;

import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;

// Every few times the mob deals damage, it casts its "Windcutter" MythicMobs skill.
public class Windcutter extends MobModifier {
	private static final int ATTACKS_REQUIRED = 3;

	public Windcutter() {
		super("Windcutter", Component.text("Windcutter"),
				Component.text("Every " + ATTACKS_REQUIRED + " times it deals damage, releases a slashing wave."), false);
	}

	@Override
	public void initialize(FightData mob) {
		int[] count = { 0 };
		boolean[] active = { false }; // Prevents the skill's own damage from counting toward itself
		mob.addMobTrigger(Trigger.DEAL_DAMAGE, (fd, in) -> {
			if (active[0]) return TriggerResult.keep();
			if (++count[0] < ATTACKS_REQUIRED) return TriggerResult.keep();
			count[0] = 0;
			LivingEntity ent = fd.getEntity();
			if (ent == null || !ent.isValid()) return TriggerResult.keep();
			active[0] = true;
			try {
				NeoRogue.mythicApi.castSkill(ent, getId());
			} finally {
				active[0] = false;
			}
			return TriggerResult.keep();
		});
	}
}
