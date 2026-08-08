package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.Particle;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ShieldsEvent;

public class ThroneOfIndifference extends Equipment {
	private static final String ID = "ThroneOfIndifference";
	private static final ParticleContainer SHIELD_GLINT = new ParticleContainer(Particle.FIREWORK).count(1)
			.spread(0, 0).offsetY(1).speed(0);
	private double shieldMultiplier;
	private int shieldPercent;

	public ThroneOfIndifference(boolean isUpgraded) {
		super(ID, "Throne of Indifference", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shieldMultiplier = isUpgraded ? 0.6 : 0.4;
		shieldPercent = (int) (shieldMultiplier * 100);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_SHIELDS, (pdata, in) -> {
			ShieldsEvent ev = (ShieldsEvent) in;
			ev.getAmountBuff().add(Buff.multiplier(data, shieldMultiplier, BuffStatTracker.ignored(this)));
			ev.getShield().makePermanent();
			SHIELD_GLINT.play(data.getPlayer(), data.getPlayer());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE,
				GlossaryTag.PASSIVE.tag(this) + ". All " + GlossaryTag.SHIELDS.tag(this)
						+ " you gain are permanent and their amounts are increased by "
						+ DescUtil.yellow(shieldPercent + "%") + ".");
	}
}