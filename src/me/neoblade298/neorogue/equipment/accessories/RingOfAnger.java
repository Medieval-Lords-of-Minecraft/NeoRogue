package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RingOfAnger extends Equipment {
	private double seconds;
	
	public RingOfAnger(boolean isUpgraded) {
		super("ringOfAnger", "Ring of Anger", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		seconds = isUpgraded ? 3 : 5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTask(id, new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (++count < seconds) return;
				count = 0;
				data.applyStatus(GenericStatusType.BASIC, "BERSERK", data.getUniqueId(), 1, -1);
				return;
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Gain a stack of " + GlossaryTag.BERSERK.tag(this) +" every <yellow>" + seconds + "</yellow> seconds.");
	}
}
