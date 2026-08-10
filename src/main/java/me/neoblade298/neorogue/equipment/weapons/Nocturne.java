package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Nocturne extends Equipment {
	private static final String ID = "Nocturne";
	private static final int INSANITY_THRESHOLD = 150, COOLDOWN_REDUCTION = 1;
	private final int insanityInterval, mana;

	public Nocturne(boolean isUpgraded) {
		super(ID, "Nocturne", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(60, 1, 0.2, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		insanityInterval = isUpgraded ? 10 : 15;
		mana = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(data.getPlayer(), data, ev.getTarget());
			return TriggerResult.keep();
		});

		ActionMeta totalInsanity = new ActionMeta();
		ActionMeta cooldownProgress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY) || ev.getStacks() <= 0) return TriggerResult.keep();
			int previous = totalInsanity.getCount();
			int current = previous + ev.getStacks();
			totalInsanity.setCount(current);
			int eligibleStacks = Math.max(0, current - INSANITY_THRESHOLD)
					- Math.max(0, previous - INSANITY_THRESHOLD);
			cooldownProgress.addCount(eligibleStacks);
			while (cooldownProgress.getCount() >= insanityInterval) {
				cooldownProgress.addCount(-insanityInterval);
				for (EquipmentInstance equipment : data.getActiveEquipment().values()) {
					equipment.reduceCooldown(COOLDOWN_REDUCTION);
				}
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.DARK)) data.addMana(mana);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD, "After applying "
				+ GlossaryTag.INSANITY.tag(this, INSANITY_THRESHOLD) + ", every subsequent "
				+ GlossaryTag.INSANITY.tag(this, insanityInterval) + " reduces all cooldowns by "
				+ DescUtil.white(COOLDOWN_REDUCTION + "s") + ". Dealing " + GlossaryTag.DARK.tag(this)
				+ " damage grants " + DescUtil.yellow(mana) + " mana.");
	}
}