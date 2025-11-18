package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EnchantedCloak extends Equipment {
	private static final String ID = "EnchantedCloak";
	private int reduc, damage;
	
	public EnchantedCloak(boolean isUpgraded) {
		super(ID, "Enchanted Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		reduc = isUpgraded ? 8 : 5;
		damage = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, reduc, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(
				UUID.randomUUID().toString(), this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage taken by <yellow>" + reduc + "</yellow>. " +
			"Increase all " + GlossaryTag.MAGICAL.tag(this) + " damage dealt by <yellow>" + damage + "</yellow>.");
	}
}
