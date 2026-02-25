package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
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

public class BurningSun extends Bow {
	private static final String ID = "BurningSun";
	private static final TargetProperties blockTp = TargetProperties.radius(3, true, TargetType.ENEMY);
	private static final TargetProperties auraTp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final ParticleContainer blockPc = new ParticleContainer(Particle.FLAME).count(50).spread(0.5, 0.5);
	private static final ParticleContainer auraPc = new ParticleContainer(Particle.FLAME).count(25).spread(5, 0.2);
	private ItemStack chargedIcon;
	private int blockDamage, burn, auraBurn;
	
	public BurningSun(boolean isUpgraded) {
		super(ID, "Burning Sun", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(80, 1, 0, 12, 0, 0.4));
		blockDamage = isUpgraded ? 150 : 100;
		burn = isUpgraded ? 15 : 10;
		auraBurn = isUpgraded ? 150 : 100;
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta passiveActive = new ActionMeta(); // Tracks if passive aura is active
		ActionMeta tickCounter = new ActionMeta(); // Counts ticks for aura application
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Shoot projectile
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new BurningSunProjectile(data, ev.getEntity().getVelocity(), this, id + slot, passiveActive, inst));
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Passive aura - apply burn every second (20 ticks) when active
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!passiveActive.getBool()) return TriggerResult.keep();
			
			// Increment tick counter
			tickCounter.addCount(1);
			if (tickCounter.getCount() >= 20) {
				tickCounter.setCount(0);
				Player p = data.getPlayer();
				
				// Play visual effect
				auraPc.play(p, p.getLocation());
				Sounds.fire.play(p, p);
				
				// Apply burn to enemies in radius
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, auraTp)) {
					FightInstance.applyStatus(ent, StatusType.BURN, data, auraBurn, -1);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CROSSBOW,
				"Projectiles have infinite " + GlossaryTag.PIERCING.tag(this) + ". " +
				"When projectiles hit a block, they explode, dealing " + 
				GlossaryTag.FIRE.tag(this, blockDamage, true) + " damage and applying " +
				GlossaryTag.BURN.tag(this, burn, true) + " to nearby enemies. " +
				"After hitting <white>8</white> walls with a projectile that also hit at least one enemy, " +
				"passively apply " + GlossaryTag.BURN.tag(this, auraBurn, true) + " to enemies within <white>5</white> blocks every second.");
		chargedIcon = item.clone();
	}
	
	private class BurningSunProjectile extends BowProjectile {
		private PlayerFightData data;
		private Player p;
		private int slot;
		private BurningSun bow;
		private ActionMeta passiveActive;
		private EquipmentInstance inst;
		private int wallHits = 0;
		private boolean hasHitEnemy = false;

		public BurningSunProjectile(PlayerFightData data, Vector v, BurningSun bow, String id, ActionMeta passiveActive, EquipmentInstance inst) {
			super(data, v, bow, id);
			this.pierce(-1); // Infinite piercing
			this.data = data;
			this.p = data.getPlayer();
			this.bow = bow;
			this.passiveActive = passiveActive;
			this.inst = inst;
			// Extract slot from id (format is "burningSun" + slot)
			this.slot = Integer.parseInt(id.replace(ID, ""));
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			// Call parent implementation for ammunition handling
			super.onHitBlock(proj, b);
			
			// Explosion effect
			Sounds.explode.play(p, proj.getLocation());
			blockPc.play(p, proj.getLocation());
			
			// Deal fire damage and apply burn to nearby enemies
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, proj.getLocation(), blockTp)) {
				FightInstance.dealDamage(new DamageMeta(data, blockDamage, DamageType.FIRE, 
						DamageStatTracker.of(ID + slot, bow)), ent);
				FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
			}
			
			// Track wall hits that also hit an enemy
			if (hasHitEnemy) {
				wallHits++;
				
				// Update icon amount to show progress
				Player freshP = data.getPlayer();
				chargedIcon.setAmount(Math.min(8, wallHits));
				inst.setIcon(chargedIcon);
				
				// Check if we should activate passive (8 walls + at least one enemy)
				if (wallHits >= 8 && !passiveActive.getBool()) {
					passiveActive.setBool(true);
					Sounds.levelup.play(freshP, freshP);
				}
			}
			
			// Reset enemy hit flag for next wall hit
			hasHitEnemy = false;
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			super.onHit(hit, hitBarrier, meta, proj);
			
			// Track that we hit at least one enemy
			hasHitEnemy = true;
		}
	}
}
