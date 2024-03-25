package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ShieldPike extends Equipment {
	private static final String ID = "shieldPike";
	private static final TargetProperties spearHit = TargetProperties.line(4, 1, TargetType.ENEMY);
	private int thorns;

	public ShieldPike(boolean isUpgraded) {
		super(
				ID, "Shield Pike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 35 : 30, 0.75, 0.3, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		thorns = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment off = data.getSessionData().getEquipment(EquipSlot.OFFHAND)[0];
		boolean hasShield = off != null && off.getItem().getType() == Material.SHIELD;
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			double damage = hasShield ? properties.get(PropertyType.DAMAGE) * 2 : properties.get(PropertyType.DAMAGE);
			data.applyStatus(StatusType.THORNS, data, thorns, -1);
			weaponSwingAndDamage(p, data, ev.getTarget(), damage);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, spearHit);
			if (targets.isEmpty())
				return TriggerResult.keep();
			double damage = hasShield ? properties.get(PropertyType.DAMAGE) * 2 : properties.get(PropertyType.DAMAGE);
			data.applyStatus(StatusType.THORNS, data, thorns, -1);
			weaponSwingAndDamage(p, data, targets.getFirst(), damage);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.TRIDENT,
				"Melee range +1. Dealing damage grants " + GlossaryTag.THORNS.tag(this, thorns, true) + "." +
				" If a shield is in offhand, this weapon does double damage."
		);
	}
}
