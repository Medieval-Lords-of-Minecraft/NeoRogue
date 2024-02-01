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

public class Smite extends Equipment {
	private int sanctified, damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).offsetForward(2).count(30).spread(4, 0.2);
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	
	public Smite(boolean isUpgraded) {
		super("smite", "Smite", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 10, 7));
		
		sanctified = isUpgraded ? 12 : 8;
		damage = isUpgraded ? 100 : 70;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP, false);
			Util.playSound(p, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, false);
			part.spawn(p);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING), ent);
				FightInstance.applyStatus(ent, StatusType.SANCTIFIED, p, sanctified, -1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, deal <white>" + damage + "</white> " + GlossaryTag.SLASHING.tag(this) + " damage in a cone in front of you and "
						+ "apply <white>" + sanctified + "</white> " + GlossaryTag.SANCTIFIED.tag(this) + ".");
	}
}
