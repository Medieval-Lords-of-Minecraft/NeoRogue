package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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

public class StoneAxe extends Equipment {
	private static final int BERSERK_THRESHOLD = 10;
	private static final TargetProperties tp = TargetProperties.cone(90, 4, false, TargetType.ENEMY);
	
	public StoneAxe(boolean isUpgraded) {
		super("stoneAxe", "Stone Axe", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 100 : 80, 0.5, 1, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			if (data.hasStatus(StatusType.BERSERK) && data.getStatus(StatusType.BERSERK).getStacks() >= BERSERK_THRESHOLD) TriggerResult.remove();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, pdata, ev.getTarget());
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			if (!data.hasStatus(StatusType.BERSERK) || data.getStatus(StatusType.BERSERK).getStacks() < BERSERK_THRESHOLD) TriggerResult.keep();
			if (pdata.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, pdata, ev.getTarget());
			FightInstance.dealDamage(properties.getDamageMeta(data), TargetHelper.getEntitiesInCone(p, tp));
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, inputs) -> {
			if (!data.hasStatus(StatusType.BERSERK) || data.getStatus(StatusType.BERSERK).getStacks() < BERSERK_THRESHOLD) TriggerResult.keep();
			if (pdata.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			FightInstance.dealDamage(properties.getDamageMeta(data), TargetHelper.getEntitiesInCone(p, tp));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_AXE, "At 20 stacks of " + GlossaryTag.BERSERK.tag(this) + ", left clicks deal damage in a cone."
				+ " Only the closest target is affected by on-hit effects.");
	}
}
