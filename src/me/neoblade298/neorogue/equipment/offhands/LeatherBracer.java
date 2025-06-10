package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class LeatherBracer extends Equipment {
	private static final String ID = "leatherBracer";
	private int instances;

	public LeatherBracer(boolean isUpgraded) {
		super(ID, "Leather Bracer", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS, EquipmentType.OFFHAND);
		instances = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new LeatherBracerInstance(this, p));
	}

	private class LeatherBracerInstance implements TriggerAction {
		private Player p;
		private int count = instances;
		private ItemStack icon;
		private Equipment eq;
		private String buffId = UUID.randomUUID().toString();

		public LeatherBracerInstance(Equipment eq, Player p) {
			this.p = p;
			icon = item.clone();
			icon.setAmount(count);
			p.getInventory().setItemInOffHand(icon);
			this.eq = eq;
		}

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			Sounds.block.play(p, p);
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, 15, BuffStatTracker.defenseBuffAlly(buffId, eq)));

			if (--count > 0) {
				icon.setAmount(count);
				p.getInventory().setItemInOffHand(icon);
				return TriggerResult.keep();
			} else {
				Sounds.breaks.play(p, p);
				p.getInventory().setItem(EquipmentSlot.OFF_HAND, null);
				return TriggerResult.remove();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER,
				"Reduces the first <yellow>" + instances + "</yellow> instances of receiving "
						+ GlossaryTag.GENERAL.tag(this) + " damage in a fight by <white>15</white>.");
	}
}
