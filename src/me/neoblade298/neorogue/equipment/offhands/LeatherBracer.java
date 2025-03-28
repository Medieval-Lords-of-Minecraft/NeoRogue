package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LeatherBracer extends Equipment {
	private static final String ID = "leatherBracer";
	private int instances;
	
	public LeatherBracer(boolean isUpgraded) {
		super(ID, "Leather Bracer", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND);
		instances = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new LeatherBracerInstance(p));
	}
	
	private class LeatherBracerInstance implements TriggerAction {
		private Player p;
		private int count = instances;
		private ItemStack icon;

		public LeatherBracerInstance(Player p) {
			this.p = p;
			icon = item.clone();
			icon.setAmount(count);
			p.getInventory().setItemInOffHand(icon);
		}

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			Sounds.block.play(p, p);

			if (--count <= 0) {
				Sounds.breaks.play(p, p);
				p.getInventory().setItem(EquipmentSlot.OFF_HAND, null);
				return TriggerResult.of(true, true);
			}
			icon.setAmount(count);
			p.getInventory().setItemInOffHand(icon);
			return TriggerResult.of(false, true);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER, "Prevents the first <yellow>" + instances + "</yellow> instances of taking damage in a fight.");
	}
}
