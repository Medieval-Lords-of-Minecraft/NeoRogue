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

public class RingOfAnger extends Equipment {
	private static final String ID = "ringOfAnger";
	private int seconds;
	
	public RingOfAnger(boolean isUpgraded) {
		super(ID, "Ring of Anger", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		seconds = isUpgraded ? 5 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTask(id, new BukkitRunnable() {
			private int count = 0;
			private boolean addedStrength = false;
			public void run() {
				if (++count < seconds) return;
				count = 0;
				data.applyStatus(StatusType.BERSERK, data, 1, -1);
				if (!addedStrength && data.getStatus(StatusType.BERSERK).getStacks() >= 8) {
					data.applyStatus(StatusType.STRENGTH, data, 5, -1 );
					addedStrength = true;
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BRICK, "Gain a stack of " + GlossaryTag.BERSERK.tag(this) +" every <yellow>" + seconds + "</yellow> seconds."
				+ " If you have at least <white>8</white> stacks after you gain a stack from the ring, gain <white>5</white> strength.");
	}
}
