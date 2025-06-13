package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VoidBracelet extends Equipment {
	private static final String ID = "voidBracelet";
	private int inc;
	public VoidBracelet(boolean isUpgraded) {
		super(ID, "Void Bracelet", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addRift(new Rift(data, p.getLocation(), 200));
			}
		}.runTaskTimer(NeoRogue.inst(), 100, 300));

		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			data.applyStatus(StatusType.INTELLECT, data, inc, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Every <white>15s</white>, drop a " + GlossaryTag.RIFT.tag(this) + " [<white>5s</white>]. Gain " +
			GlossaryTag.INTELLECT.tag(this, inc, true) + " every time you create a rift.");
	}
}
