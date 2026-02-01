package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DensityOrb extends Equipment {
	private static final String ID = "DensityOrb";
	private static final ParticleContainer pc = new ParticleContainer(Particle.END_ROD);
	private static final SoundContainer shoot = new SoundContainer(Sound.ENTITY_SHULKER_SHOOT),
		expl = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_HURT);
	private TargetProperties tp;
	private Circle circ;
	private int shields;

	
	public DensityOrb(boolean isUpgraded) {
		super(ID, "Density Orb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 16, 0, isUpgraded ? 5 : 3));
		properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.AREA_OF_EFFECT);
		tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false);
		circ = new Circle(tp.range);
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup group = new ProjectileGroup(new DensityOrbProjectile(data));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			group.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL, "On cast, throw an orb that explodes on an enemy or block, granting all nearby enemies " + 
		DescUtil.potion("Slowness", 2, 3) + ", creating a " + GlossaryTag.RIFT.tag(this) + " [<white>15s</white>], and granting "
		+ GlossaryTag.SHIELDS.tag(this, shields, true) + " if it hits at least one enemy.");
	}

	private class DensityOrbProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;

		public DensityOrbProjectile(PlayerFightData data) {
			super(1, 10, 2);
			this.gravity(0.05);
			this.arc(0.5);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			slowArea(hit.getEntity().getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			slowArea(b.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			shoot.play(p, p);
		}

		private void slowArea(Location loc) {
			while (loc.getBlock().getType().isAir()) {
				loc.add(0, -1, 0);
			}
			expl.play(p, loc);
			circ.play(p, pc, loc, LocalAxes.xz(), null);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInRadius(p, loc, tp);
			for (LivingEntity ent : trgs) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
			}
			data.addRift(new Rift(data, loc.add(0, 1, 0), 300));
			if (!trgs.isEmpty()) data.addPermanentShield(p.getUniqueId(), shields);
		}
		
	}
}
