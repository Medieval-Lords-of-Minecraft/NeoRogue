package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EngineersCap extends Equipment {
	private static final String ID = "engineersCap";
	private int damage, dec;
	private double damageActual;
	
	public EngineersCap(boolean isUpgraded) {
		super(ID, "Engineer's Cap", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		damage = isUpgraded ? 15 : 10;
		damageActual = damage * 0.01;
		dec = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, dec, StatTracker.defenseBuffAlly(this)));
		data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL, DamageOrigin.TRAP), Buff.multiplier(data, damageActual, StatTracker.damageBuffAlly(this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Decrease " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.yellow(dec) + ". Any " + GlossaryTag.TRAP.tagPlural(this) +
				" you lay will deal " + DescUtil.yellow(damage + "%") + " more damage.");
	}
}
