package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class DeadMansPlate extends Equipment {
	private static final String ID = "DeadMansPlate";
	private int reduction = 2, closeReduction, range = 6;

	public DeadMansPlate(boolean isUpgraded) {
		super(ID, "Dead Man's Plate", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		closeReduction = isUpgraded ? 6 : 4;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT),
				Buff.increase(data, reduction, StatTracker.defenseBuffAlly(id + slot, this)));
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent event = (ReceiveDamageEvent) in;
			if (!event.getMeta().containsType(DamageCategory.DIRECT) || event.getDamager() == null
					|| event.getDamager().getEntity().getWorld() != data.getPlayer().getWorld()
					|| event.getDamager().getEntity().getLocation().distanceSquared(data.getPlayer().getLocation()) > range * range) {
				return TriggerResult.keep();
			}
			event.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.increase(data, closeReduction, StatTracker.defenseBuffAlly(id + "-close-" + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this)
				+ " damage taken by " + DescUtil.val(reduction) + ", plus another " + DescUtil.val(closeReduction)
				+ " when within " + DescUtil.val(range) + " blocks of the damage source.");
	}
}