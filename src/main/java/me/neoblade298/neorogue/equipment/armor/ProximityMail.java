package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class ProximityMail extends Equipment {
	private static final String ID = "ProximityMail";
	private int reduction = 1, statusStacks = 2, duration;
	private int range = 6, cooldown = 2;

	public ProximityMail(boolean isUpgraded) {
		super(ID, "Proximity Mail", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		duration = isUpgraded ? 14 : 10;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta internalCooldown = new ActionMeta();
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT),
				Buff.increase(data, reduction, StatTracker.defenseBuffAlly(id + slot, this)));
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			long now = System.currentTimeMillis();
			if (now < internalCooldown.getTime()
					|| event.getTarget().getLocation().distanceSquared(data.getPlayer().getLocation()) > range * range) {
				return TriggerResult.keep();
			}
			internalCooldown.setTime(now + cooldown * 1000L);
			data.applyStatus(StatusType.PROTECT, data, statusStacks, duration * 20, this);
			data.applyStatus(StatusType.SHELL, data, statusStacks, duration * 20, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this) + " damage taken by "
				+ DescUtil.val(reduction) + ". Damaging an enemy within " + DescUtil.val(range) + " blocks grants "
				+ GlossaryTag.PROTECT.tag(this, statusStacks) + " and " + GlossaryTag.SHELL.tag(this, statusStacks)
				+ " [" + DescUtil.val(duration + "s") + "]. " + DescUtil.val(cooldown + "s") + " cooldown.");
	}
}