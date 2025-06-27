package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class MagicSpear extends Equipment {
	private static final String ID = "magicSpear";
	private static final TargetProperties spearHit = TargetProperties.line(4, 1, TargetType.ENEMY);
	private int damage;

	public MagicSpear(boolean isUpgraded) {
		super(ID, "Magic Spear", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(0, 1, isUpgraded ? 50 : 40, 0.75, 0.2, DamageType.EARTHEN,
						Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
		damage = isUpgraded ? 120 : 90;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget(), properties.get(PropertyType.DAMAGE)
					+ (FightInstance.getFightData(ev.getTarget()).hasStatus(StatusType.CONCUSSED) ? damage : 0));
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!data.canBasicAttack())
				return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, spearHit);
			if (targets.isEmpty())
				return TriggerResult.keep();
			weaponSwingAndDamage(p, data, targets.getFirst(), properties.get(PropertyType.DAMAGE)
					+ (FightInstance.getFightData(targets.getFirst()).hasStatus(StatusType.CONCUSSED) ? damage : 0));
			return TriggerResult.keep();
		});
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT, "Melee range +1. Deals an additional " + DescUtil.yellow(damage)
				+ " damage if enemy is " + GlossaryTag.CONCUSSED.tag(this) + ".");
	}
}
