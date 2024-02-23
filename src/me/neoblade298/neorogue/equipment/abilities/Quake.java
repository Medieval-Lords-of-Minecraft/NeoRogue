package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Quake extends Equipment {
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private int concussed, damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.CLOUD).spread(tp.range, 0.2).count(50);
	
	public Quake(boolean isUpgraded) {
		super("quake", "Quake", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 30, 8, tp.range));
		
		concussed = isUpgraded ? 16 : 12;
		damage = isUpgraded ? 130 : 100;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_WARDEN_ATTACK_IMPACT, false);
			part.spawn(p);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN), ent);
				FightInstance.applyStatus(ent, StatusType.CONCUSSED, p, concussed, -1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, deal <yellow>" + damage + "</yellow> " + GlossaryTag.EARTHEN.tag(this) + " damage to all "
						+ "enemies in the radius and apply <yellow>" + concussed + "</yellow> " + GlossaryTag.CONCUSSED.tag(this) + ".");
	}
}
