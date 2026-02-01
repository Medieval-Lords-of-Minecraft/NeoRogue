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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
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
	private static final String ID = "StoneAxe";
	private static final int BERSERK_THRESHOLD = 10;
	private static final TargetProperties tp = TargetProperties.cone(60, 3, false, TargetType.ENEMY);
	
	public StoneAxe(boolean isUpgraded) {
		super(ID, "Stone Axe", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 100 : 80, 0.5, 0.4, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Player p = data.getPlayer();
			if (data.hasStatus(StatusType.BERSERK) && data.getStatus(StatusType.BERSERK).getStacks() >= BERSERK_THRESHOLD) return TriggerResult.remove();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, pdata, ev.getTarget());
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			Player p = data.getPlayer();
			if (!data.canBasicAttack()) return TriggerResult.keep();
			if (!data.hasStatus(StatusType.BERSERK) || data.getStatus(StatusType.BERSERK).getStacks() < BERSERK_THRESHOLD) return TriggerResult.keep();
			if (!pdata.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			weaponSwing(p, data);
			LinkedList<LivingEntity> ents = TargetHelper.getEntitiesInCone(p, tp);
			if (ents.isEmpty()) return TriggerResult.keep();
			LivingEntity first = ents.peekFirst();
			weaponSwingAndDamage(p, data, first);
			for (LivingEntity ent : ents) {
				if (ent == first) continue;
				FightInstance.dealDamage(new DamageMeta(data, this, true, DamageStatTracker.of(id + slot, this)), ent);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_AXE, "At " + DescUtil.white(BERSERK_THRESHOLD) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", left clicks deal damage in a cone."
				+ " Only the closest target is affected by on-hit effects.");
	}
}
