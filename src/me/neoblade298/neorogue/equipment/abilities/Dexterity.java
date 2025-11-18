package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class Dexterity extends Equipment {
	private static final String ID = "Dexterity";
	private int damage;
	
	public Dexterity(boolean isUpgraded) {
		super(ID, "Dexterity", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.applyStatus(StatusType.EVADE, data, 1, -1);
		data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(buffId, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Passive. Increase all " + GlossaryTag.PHYSICAL.tag(this, damage, true) + " damage by " + DescUtil.white(damage) + ". " +
				"Start fights with " + GlossaryTag.EVADE.tag(this, 1, false) + ".");
	}
}
