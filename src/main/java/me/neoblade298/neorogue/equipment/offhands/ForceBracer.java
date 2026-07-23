package me.neoblade298.neorogue.equipment.offhands;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class ForceBracer extends Equipment {
	private static final String ID = "ForceBracer";
	private int instances, multStr, strength, berserk;
	private double mult;

	public ForceBracer(boolean isUpgraded) {
		super(ID, "Force Bracer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.OFFHAND);
		instances = 2;
		mult = isUpgraded ? 0.3 : 0.2;
		multStr = (int) (mult * 100);
		strength = isUpgraded ? 15 : 10;
		berserk = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, new ForceBracerInstance(data.getPlayer(), this));
	}

	private class ForceBracerInstance implements TriggerAction {
		private int count = instances;
		private ItemStack icon;
		private Equipment eq;
		private String buffId = UUID.randomUUID().toString();

		public ForceBracerInstance(Player p, Equipment eq) {
			icon = item.clone();
			icon.setAmount(count);
			p.getInventory().setItemInOffHand(icon);
			this.eq = eq;
		}

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) inputs;
			if (ev.isNullified()) {
				return TriggerResult.keep();
			}
			Player p = data.getPlayer();
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
				data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id, eq)));
				data.applyStatus(StatusType.STRENGTH, data, strength, -1, ForceBracer.this);
				return TriggerResult.remove();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE,
				"Reduces the first " + DescUtil.val(instances) + " instances of receiving "
						+ GlossaryTag.GENERAL.tag(this) + " damage in a fight by " + DescUtil.val(15) + ". Upon breaking, grants a " +
						DescUtil.val(multStr + "%") + " damage buff, " + GlossaryTag.STRENGTH.tag(this, strength) + ", and " + 
						GlossaryTag.BERSERK.tag(this, berserk) + ".");
	}
}
