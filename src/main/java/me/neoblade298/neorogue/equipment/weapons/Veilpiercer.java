package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Veilpiercer extends Equipment {
	private static final String ID = "Veilpiercer";

	public Veilpiercer(boolean isUpgraded) {
		super(ID, "Veilpiercer", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 200 : 160, 0.2, 0.2, DamageType.PIERCING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ItemStack cooldownIcon = item.clone().withType(Material.WOODEN_SWORD);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(data.getPlayer(), data, ev.getTarget());
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.isStatus(StatusType.STEALTH) && ev.getTarget() == data && ev.getStacks() > 0) {
				resetIfCurrent(data, inst, cooldownIcon);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			resetIfCurrent(data, inst, cooldownIcon);
			return TriggerResult.keep();
		});
	}

	private void resetIfCurrent(PlayerFightData data, StandardEquipmentInstance inst, ItemStack cooldownIcon) {
		long cooldownMillis = getResetCooldownSeconds() * 1000L;
		if (inst.getTime() + cooldownMillis > System.currentTimeMillis()
				|| !data.wasLastBasicAttackSetBy(EquipSlot.HOTBAR, this)) return;

		data.resetBasicAttackCooldown(EquipSlot.HOTBAR);
		inst.setTime(System.currentTimeMillis());
		inst.setIcon(cooldownIcon);
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				inst.setIcon(item);
			}
		}.runTaskLater(NeoRogue.inst(), getResetCooldownSeconds() * 20L));
	}

	private int getResetCooldownSeconds() {
		return (int) Math.ceil(1 / properties.get(PropertyType.ATTACK_SPEED));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD, "Gaining " + GlossaryTag.STEALTH.tag(this) + " or "
				+ GlossaryTag.DASH.tag(this) + "ing resets this weapon's basic attack cooldown (if it was your most recent basic attack). "
				+ "This effect has a " + DescUtil.duration(getResetCooldownSeconds()) + " cooldown.");
	}
}