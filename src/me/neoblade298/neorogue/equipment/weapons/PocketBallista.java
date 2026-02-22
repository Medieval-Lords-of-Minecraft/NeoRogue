package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class PocketBallista extends Bow {
	private static final String ID = "PocketBallista";
	private static final TargetProperties tp = TargetProperties.radius(3, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION);
	private int damage;
	
	public PocketBallista(boolean isUpgraded) {
		super(ID, "Pocket Ballista", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(80, 1, 0, 12, 0, 0.4));
		damage = isUpgraded ? 150 : 100;
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new PocketBallistaProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Projectiles have infinite " + GlossaryTag.PIERCING.tag(this) + ". " +
				"When projectiles hit a block, they explode, dealing " + 
				GlossaryTag.BLUNT.tag(this, damage, true) + " damage to nearby enemies.");
	}
	
	private class PocketBallistaProjectile extends BowProjectile {
		private PlayerFightData data;
		private Player p;
		private int slot;
		private PocketBallista bow;

		public PocketBallistaProjectile(PlayerFightData data, Vector v, PocketBallista bow, String id) {
			super(data, v, bow, id);
			this.pierce(-1); // Infinite piercing
			this.data = data;
			this.p = data.getPlayer();
			this.bow = bow;
			// Extract slot from id (format is "PocketBallista" + slot)
			this.slot = Integer.parseInt(id.replace(ID, ""));
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			// Call parent implementation for ammunition handling
			super.onHitBlock(proj, b);
			
			// Explosion effect
			Sounds.explode.play(p, proj.getLocation());
			pc.play(p, proj.getLocation());
			
			// Deal damage to nearby enemies
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, proj.getLocation(), tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, 
						DamageStatTracker.of(ID + slot, bow)), ent);
			}
		}
	}
}
