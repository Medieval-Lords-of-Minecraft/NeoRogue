package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SlowingOrb extends Equipment {
	private static final String ID = "SlowingOrb";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ITEM_SLIME);
	private static final SoundContainer shoot = new SoundContainer(Sound.ENTITY_SHULKER_SHOOT),
		expl = new SoundContainer(Sound.ENTITY_SLIME_DEATH);
	private TargetProperties tp;
	private Circle circ;
	
	public SlowingOrb(boolean isUpgraded) {
		super(ID, "Slowing Orb", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(5, 0, isUpgraded ? 12 : 15, 0, isUpgraded ? 5 : 3));
		properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.AREA_OF_EFFECT);
		tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false);
		circ = new Circle(tp.range);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(CalculatingGaze.get(), DensityOrb.get());
		addReforge(Intuition.get(), ReckoningOrb.get(), ElectricOrb.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup group = new ProjectileGroup(new SlowingOrbProjectile());
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			group.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL, "On cast, throw an orb that explodes on an enemy or block, granting all nearby enemies " + 
		DescUtil.potion("Slowness", 2, 3) + ".");
	}

	private class SlowingOrbProjectile extends Projectile {

		public SlowingOrbProjectile() {
			super(1, 10, 2);
			this.gravity(0.05);
			this.arc(0.5);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play((Player) proj.getOwner().getEntity(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player p = (Player) proj.getOwner().getEntity();
			slowArea(p, hit.getEntity().getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Player p = (Player) proj.getOwner().getEntity();
			slowArea(p, b.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = (Player) proj.getOwner().getEntity();
			shoot.play(p, p);
		}

		private void slowArea(Player p, Location loc) {
			while (loc.getBlock().getType().isAir()) {
				loc.add(0, -1, 0);
			}
			expl.play(p, loc);
			circ.play(p, pc, loc, LocalAxes.xz(), null);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
			}
		}
		
	}
}
