package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RingOfFortitude extends Equipment {
	private int seconds;
	
	public RingOfFortitude(boolean isUpgraded) {
		super("ringOfFortitude", "Ring Of Fortitude", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTask(id, new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (++count < seconds) return;
				count = 0;
				data.applyStatus(StatusType.BERSERK, data.getUniqueId(), 1, -1);
				return;
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Your basic attacks additionally deal your current " + GlossaryTag.SHIELDS.tag(this)
				+ " as " + GlossaryTag.BLUNT.tag(this) + " damage.");
	}
}
