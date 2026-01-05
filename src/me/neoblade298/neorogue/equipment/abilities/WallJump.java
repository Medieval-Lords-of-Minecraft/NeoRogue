package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class WallJump extends Equipment {
	private static final String ID = "WallJump";
	private static final ParticleContainer dashParticle = new ParticleContainer(Particle.CLOUD)
		.count(5).spread(0.3, 0.3);
	private static final ParticleContainer jumpParticle = new ParticleContainer(Particle.FIREWORK)
		.count(10).spread(0.5, 0.5);
	private static final ParticleContainer lineParticle = new ParticleContainer(Particle.CRIT)
		.count(5).spread(0.2, 0.2);
	private static final TargetProperties tp = TargetProperties.line(12, 2, TargetType.ENEMY);
	
	private int damage;

	public WallJump(boolean isUpgraded) {
		super(ID, "Wall Jump", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 12, 0));
		damage = isUpgraded ? 250 : 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		WallJumpInstance inst = new WallJumpInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
	}

	private class WallJumpInstance extends EquipmentInstance {
		private boolean canRecast = false;
		private Vector direction;
		
		public WallJumpInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				
				// Recast: dash long distance and deal damage in a line
				if (canRecast) {
					Sounds.jump.play(p, p);
					
					// Dash in stored direction
					Vector dashVec = direction.clone().multiply(1.5);
					p.setVelocity(dashVec);
					
					// Deal line damage
					Location start = p.getLocation().add(0, 1, 0);
					Location end = start.clone().add(direction.clone().multiply(tp.range));
					
					ParticleUtil.drawLine(p, lineParticle, start, end, 0.3);
					jumpParticle.play(p, p.getLocation());
					
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING, 
							DamageStatTracker.of(id + slot, eq)), ent);
					}
					
					// Reset state
					canRecast = false;
					direction = null;
					setIcon(item);
					
					return TriggerResult.keep();
				}
				
				// Initial cast: dash forward and check for wall
				direction = p.getEyeLocation().getDirection().setY(0).normalize();
				data.dash();
				dashParticle.play(p, p.getLocation());
				Sounds.equip.play(p, p);
				
				// Check for wall collision after dash completes
				new BukkitRunnable() {
					int ticks = 0;
					
					public void run() {
						ticks++;
						
						// Check for wall hit
						if (hitWall(p)) {
							// Enable recast
							canRecast = true;
							ItemStack recastIcon = item.clone();
							recastIcon.withType(Material.PHANTOM_MEMBRANE);
							setIcon(recastIcon);
							setCooldown(0);
							Sounds.block.play(p, p);
							this.cancel();
						}
						
						// Stop checking after dash should be complete (10 ticks)
						if (ticks >= 10) {
							this.cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 0L, 1L);
				
				return TriggerResult.keep();
			};
		}
		
		private boolean hitWall(Player p) {
			Location loc = p.getLocation();
			Block forward = loc.clone().add(direction.clone().multiply(0.5)).getBlock();
			
			// Check forward and up
			for (int i = 0; i < 2; i++) {
				if (forward.isCollidable()) return true;
				forward = forward.getRelative(BlockFace.UP);
			}
			
			// Check immediate surroundings for walls
			Block current = loc.getBlock();
			for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
				Block check = current.getRelative(face);
				for (int i = 0; i < 2; i++) {
					if (check.isCollidable()) return true;
					check = check.getRelative(BlockFace.UP);
				}
			}
			
			return false;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				GlossaryTag.DASH.tag(this) + " forward. If you hit a wall, recast to " + GlossaryTag.DASH.tag(this) + " a long distance and deal "
				+ GlossaryTag.SLASHING.tag(this, damage, true) + " damage in a line.");
	}
}
