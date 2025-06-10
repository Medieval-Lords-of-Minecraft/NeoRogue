package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
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

public class Resourcefulness extends Equipment {
	private static final String ID = "resourcefulness";
	private int poison, damage;
	
	public Resourcefulness(boolean isUpgraded) {
		super(ID, "Resourcefulness", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
				poison = isUpgraded ? 10 : 5;
				damage = 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		Equipment eq = this;
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(buffId, eq)));
			}
		}.runTaskLater(NeoRogue.inst(), 100L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHEST,
				"Passive. Increase all " + GlossaryTag.POISON.tag(this) + " application stacks by " + DescUtil.yellow(poison) + ". " +
				"Increase all " + GlossaryTag.PHYSICAL.tag(this) + " damage by " + DescUtil.yellow(damage) + " for <white>10s</white> after a fight starts.");
	}
}
