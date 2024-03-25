package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Glowstick extends Equipment {
	private static final String ID = "glowstick";
	private int buffPercent;
	private boolean used = false;

	public Glowstick(boolean isUpgraded) {
		super(ID, "Glowstick", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
		buffPercent = isUpgraded ? 100 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger Bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK, (pdata, inputs) -> {
			if (used)
				return TriggerResult.keep();
			used = true;

			double flatGain = data.getManaRegen() * buffPercent / 100.0;
			data.addManaRegen(flatGain);
			data.addTask(id, new BukkitRunnable() {
				@Override
				public void run() {
					data.addManaRegen(-flatGain);
				}
			}.runTaskLater(NeoRogue.inst(), 200L));

			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK,
				"Once per fight, right click to increase your mana regen by <yellow>" + buffPercent
						+ "%</yellow> for 10 seconds."
		);
	}
}
