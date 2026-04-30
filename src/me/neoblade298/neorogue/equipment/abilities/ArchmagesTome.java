package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;

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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ArchmagesTome extends Equipment {
	private static final String ID = "ArchmagesTome";
	private int damage;
	private double regen;
	
	public ArchmagesTome(boolean isUpgraded) {
		super(ID, "Archmage's Tome", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 45 : 30;
		regen = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(data, damage, 0, StatTracker.damageBuffAlly(buffId, this)));
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.5) {
				data.addMana(regen);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENCHANTED_BOOK,
				"Passive. Increase all " + GlossaryTag.MAGICAL.tag(this) + " damage by " + DescUtil.yellow(damage) + ". " +
				"While below <white>50%</white> mana, mana regen is increased by " + DescUtil.yellow(regen) + ".");
	}
}
