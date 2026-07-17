package me.neoblade298.neorogue.session.fight.modifier;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import net.kyori.adventure.text.Component;

// Reduces magical damage the mob takes by 20%.
public class ManaBarrier extends MobModifier {
	private static final double REDUCTION = 0.2;

	public ManaBarrier() {
		super("ManaBarrier", Component.text("Mana Barrier"),
				Component.text("Takes 20% less magical damage."), false);
	}

	@Override
	public void initialize(FightData mob) {
		mob.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL),
				new Buff(mob, 0, REDUCTION, BuffStatTracker.ignored(id)));
	}
}
