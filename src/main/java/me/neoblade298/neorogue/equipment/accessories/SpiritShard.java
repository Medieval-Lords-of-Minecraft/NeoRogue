package me.neoblade298.neorogue.equipment.accessories;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpiritShard extends Equipment {
	private static final String ID = "SpiritShard";
	private static final int DURATION = 3;
	private double mana;

	public SpiritShard(boolean isUpgraded) {
		super(ID, "Spirit Shard", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		mana = isUpgraded ? 2.5 : 1.5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// Holds the pending expiration task; non-null means the mana regen buff is currently active.
		final BukkitTask[] active = new BukkitTask[1];
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (active[0] == null) {
				data.addManaRegen(mana); // Apply once; nonstacking
			} else {
				active[0].cancel(); // Already active, refresh the duration instead of stacking
			}
			BukkitTask task = new BukkitRunnable() {
				@Override
				public void run() {
					data.addManaRegen(-mana);
					active[0] = null;
				}
			}.runTaskLater(NeoRogue.inst(), DURATION * 20L);
			active[0] = task;
			data.addTask(task);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_BANNER, "When you cast an ability, increase your mana regen by "
				+ DescUtil.val(mana) + " for " + DescUtil.duration(DURATION) + ". Does not stack.");
	}
}
