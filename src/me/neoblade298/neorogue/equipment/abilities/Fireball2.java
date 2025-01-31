package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fireball2 extends Equipment {
	private static final String ID = "fireball2";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FLAME).count(5).spread(0.3, 0.3),
		explode = new ParticleContainer(Particle.EXPLOSION).count(3).spread(0.5, 0.5);
	private static final TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);

	private int damage, burn;
	
	public Fireball2(boolean isUpgraded) {
		super(
				ID , "Fireball II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(40, 0, 12, 10, tp.range));
		damage = isUpgraded ? 360 : 240;
		burn = 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FireballProjectile(data));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata ,in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					proj.start(data);
				}
			});
			return TriggerResult.keep();
		}));
	}
	
	private class FireballProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;

		public FireballProjectile(PlayerFightData data) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			explode(hit.getEntity().getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			explode(proj.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
		}

		private void explode(Location loc) {
			Sounds.explode.play(p, loc);
			explode.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				if (ent instanceof Player || !(ent instanceof LivingEntity)) continue;
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
				FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
			GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before launching a fireball that deals " + GlossaryTag.FIRE.tag(this, damage, true) + " damage and applies " +
			GlossaryTag.BURN.tag(this, burn, false) + " in an area.");
	}
}
