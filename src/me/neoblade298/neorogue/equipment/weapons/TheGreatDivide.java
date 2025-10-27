package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class TheGreatDivide extends Equipment {
	private static final String ID = "theGreatDivide";
	private static final TargetProperties tp = TargetProperties.line(6, 2, TargetType.ENEMY);
	private static final ParticleContainer part = new ParticleContainer(Particle.CLOUD).spread(1, 0.1).speed(0.02).count(20);
	private int damage, concussed;

	public TheGreatDivide(boolean isUpgraded) {
		super(
				ID, "The Great Divide", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(100, 0.5, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT)
				.add(PropertyType.RANGE, tp.range)
		);
		damage = isUpgraded ? 325 : 250;
		concussed = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget());
			
			if (am.addCount(1) < 3) return TriggerResult.keep();

			// fissure
			Sounds.explode.play(p, p);
			Vector forward = p.getEyeLocation().getDirection().setY(0).normalize().multiply(tp.range);
			Location end = p.getLocation().add(forward);
			ParticleUtil.drawLine(p, part, p.getLocation(), end, 0.5);
			
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, p.getLocation(), end, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.getFightData(ent.getUniqueId()).applyStatus(StatusType.CONCUSSED, data, concussed, -1);
				FightInstance.knockback(ent, new Vector(0, 0.4, 0));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MACE,
				"Every <white>3rd</white> hit, deal " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage, apply "
						+ GlossaryTag.CONCUSSED.tag(this, concussed, true) + ", and knock up in a line.");
	}
}
