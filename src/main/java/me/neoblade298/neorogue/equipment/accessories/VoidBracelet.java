package me.neoblade298.neorogue.equipment.accessories;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VoidBracelet extends Equipment {
	private static final String ID = "VoidBracelet";
	private int inc;
	public VoidBracelet(boolean isUpgraded) {
		super(ID, "Void Bracelet", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				Player p = data.getPlayer();
				data.addRift(new Rift(data, p.getLocation(), 200));
			}
		}.runTaskLater(NeoRogue.inst(), 100));

		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			data.applyStatus(StatusType.INTELLECT, data, inc, -1, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "After " + DescUtil.val("5s") + ", drop a " + GlossaryTag.RIFT.tag(this) + " [<white>10s</white>]. Gain " +
			GlossaryTag.INTELLECT.tag(this, inc) + " every time you create a rift.");
	}
}
