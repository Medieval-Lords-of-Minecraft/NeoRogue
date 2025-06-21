package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ReckoningOrb extends Equipment {
	private static final String ID = "reckoningOrb";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ITEM_SLIME);
	private static final SoundContainer shoot = new SoundContainer(Sound.ENTITY_SHULKER_SHOOT),
		expl = new SoundContainer(Sound.ENTITY_SLIME_DEATH);
	private TargetProperties tp;
	private Circle circ;
	private double mult;
	private int multStr;
	
	public ReckoningOrb(boolean isUpgraded) {
		super(ID, "Reckoning Orb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 15, 0, 5));
		tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false);
		circ = new Circle(tp.range);
		mult = isUpgraded ? 1 : 0.5;
		multStr = (int) (100 * mult);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup group = new ProjectileGroup(new ReckoningOrbProjectile(data, UUID.randomUUID().toString(), this));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			group.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL, "On cast, throw an orb that explodes on an enemy or block, granting all nearby enemies " + 
		DescUtil.potion("Slowness", 2, 3) + " and increasing all damage dealt against them by " + DescUtil.yellow(multStr + "%") + " [<white>5s</white>].");
	}

	private class ReckoningOrbProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private String buffId;
		private ReckoningOrb eq;

		public ReckoningOrbProjectile(PlayerFightData data, String buffId, ReckoningOrb eq) {
			super(1, 10, 2);
			this.gravity(0.05);
			this.arc(0.5);
			this.data = data;
			this.p = data.getPlayer();
			this.buffId = buffId;
			this.eq = eq;
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
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
				FightData fd = FightInstance.getFightData(ent);
				fd.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, -mult, BuffStatTracker.damageDebuffEnemy(buffId, eq, false)), 100);
			}
		}
		
	}
}
