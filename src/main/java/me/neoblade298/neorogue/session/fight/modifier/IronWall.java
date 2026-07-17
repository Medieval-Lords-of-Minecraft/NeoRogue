package me.neoblade298.neorogue.session.fight.modifier;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.MobModifier;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import net.kyori.adventure.text.Component;

// Reduces physical damage the mob takes by 20%.
public class IronWall extends MobModifier {
	private static final double REDUCTION = 0.2;

	public IronWall() {
		super("IronWall", Component.text("Iron Wall"),
				Component.text("Takes 20% less physical damage."), false);
	}

	@Override
	public void initialize(FightData mob) {
		mob.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
				new Buff(mob, 0, REDUCTION, BuffStatTracker.ignored(id)));
	}
}
