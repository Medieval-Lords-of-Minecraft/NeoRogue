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

public class FlashMark extends Equipment {
	private static final String ID = "FlashMark";
	private static final TargetProperties tp = TargetProperties.line(30, 1.5, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.ELECTRIC_SPARK).count(25).spread(0.5, 0.5);
	private int damage, electrified;
	
	public FlashMark(boolean isUpgraded) {
		super(ID, "Flash Mark", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 8, 30));
		damage = isUpgraded ? 250 : 180;
		electrified = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			// Launch projectile toward the block
			ProjectileGroup proj = new ProjectileGroup(new FlashMarkProjectile(data, slot, this));
			proj.start(data);
			Sounds.shoot.play(p, p);
			
			return TriggerResult.keep();
		}));
	}
	
	private class FlashMarkProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Location startLoc;
		private int slot;
		private Equipment eq;

		public FlashMarkProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(properties.get(PropertyType.RANGE), 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.p = data.getPlayer();
			this.slot = slot;
			this.eq = eq;
			
			blocksPerTick(1.5);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}

		@Override
		public void onStart(ProjectileInstance proj) {

		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location endLoc = b.getLocation();
			
			// Deal damage and apply electrified to all enemies in line
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, startLoc, endLoc, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, eq)), ent);
				FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, electrified, -1);
			}
			
			// Dash to the block location
			Location playerLoc = p.getLocation();
			playerLoc.setDirection(endLoc.toVector().subtract(startLoc.toVector()).normalize());
			p.teleport(playerLoc);
			data.dash(endLoc.toVector().subtract(startLoc.toVector()).normalize());
			
			Sounds.equip.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On cast, throw a projectile. If it hits a block, " + GlossaryTag.DASH.tag(this) + " in that direction, " +
				"dealing " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + " to enemies in a line between you and the block.");
	}
}
