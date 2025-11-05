package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Rushdown extends Equipment {
	private static final String ID = "rushdown";
	private int secs;
	private double inc = 1.5;
	
	public Rushdown(boolean isUpgraded) {
		super(ID, "Rushdown", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		secs = isUpgraded ? 40 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addStaminaRegen(inc);
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addStaminaRegen(-inc);
				Util.msg(p, hoverable.append(Component.text(" has expired", NamedTextColor.GRAY)));
			}
		}.runTaskLater(NeoRogue.inst(), 20L * secs));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RED_BANNER,
				"Passive. Increase stamina regen by <white>" + inc + "</white> for the first <yellow>"
				+ secs + "</yellow> seconds of a fight.");
	}
}
