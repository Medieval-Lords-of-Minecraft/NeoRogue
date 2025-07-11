package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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

public class RighteousLance extends Equipment {
	private static final String ID = "righteousLance";
	private static final ParticleContainer lancePart = new ParticleContainer(Particle.ELECTRIC_SPARK).count(3).spread(0.1, 0.1);
	private static final TargetProperties regHit = TargetProperties.line(4, 1, TargetType.ENEMY),
			lanceHit = TargetProperties.line(6, 1, TargetType.ENEMY);
	private int sanct;

	public RighteousLance(boolean isUpgraded) {
		super(
				ID, "Righteous Lance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(80, 0.7, 0.3, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		sanct = isUpgraded ? 55 : 35;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget());
			FightInstance.applyStatus(ev.getTarget(), StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, regHit);
			if (targets.isEmpty())
				return TriggerResult.keep();
			weaponSwingAndDamage(p, data, targets.getFirst());
			FightInstance.applyStatus(targets.getFirst(), StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.THROW_TRIDENT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, lanceHit);
			weaponSwing(p, data);
			Location start = p.getLocation().add(0, 1, 0);
			Vector v = p.getLocation().getDirection().setY(0).normalize().multiply(lanceHit.range);
			ParticleUtil.drawLine(p, lancePart, p.getLocation().add(0, 1, 0), start.clone().add(v), 0.5);
			if (targets.isEmpty())
				return TriggerResult.keep();
			int stacks = FightInstance.getFightData(targets.getFirst()).getStatus(StatusType.SANCTIFIED).getStacks();
			DamageMeta dm = new DamageMeta(data, this, true, DamageStatTracker.of(id + slot, this)).setKnockback(-0.5);
			dm.addDamageSlice(new DamageSlice(data, stacks, DamageType.LIGHT, DamageStatTracker.of(id + slot, this)));
			FightInstance.dealDamage(dm, targets.getFirst());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.TRIDENT,
				"Melee range +1, applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ". Throwing the weapon instead increases its range by <white>2</white> and deals additional "
				+ GlossaryTag.LIGHT.tag(this) + " damage equal to the " + GlossaryTag.SANCTIFIED.tag(this) + " stacks on the target."
		);
	}
}
