package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class BlastStep extends Equipment {
	private static final String ID = "blastStep";
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	private static final Vector kb = new Vector(0, 2, 0);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	private int damage, inc;
	
	public BlastStep(boolean isUpgraded) {
		super(ID, "Blast Step", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 15, 12, 5));
		damage = isUpgraded ? 100 : 70;
		inc = isUpgraded ? 90 : 60;

	}

	@Override
	public void setupItem() {
		item = createItem(Material.TNT_MINECART,
				"On cast, gain " + DescUtil.potion("Speed", 1, 3) + ". Deal " + GlossaryTag.BLUNT.tag(this, damage, true) + " damage " +
				"and knock up all enemies in a cone in front of you. Enemies hit take an additional " + GlossaryTag.BLUNT.tag(this, inc, true) + " [<white>3s</white>] " +
				"from projectiles fired within <white>5</white> blocks of them." );
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
			cone.play(p, pc, p.getLocation(), LocalAxes.usingGroundedEyeLocation(p), pc);
			Sounds.explode.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT), ent);
				FightInstance.knockback(ent, kb);
				FightData fd = FightInstance.getFightData(ent);
				fd.applyStatus(
					Status.createByGenericType(GenericStatusType.BASIC, "blastStep-" + p.getName(), fd, true), data, 1, 60);
			}
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd.hasStatus("blastStep-" + p.getName()) && ev.getMeta().getProjectile().getOrigin().distanceSquared(ev.getTarget().getLocation()) <= 25) {
				ev.getMeta().addDamageSlice(new DamageSlice(data, inc, DamageType.BLUNT));
			}
			return TriggerResult.keep();
		});
	}
}
