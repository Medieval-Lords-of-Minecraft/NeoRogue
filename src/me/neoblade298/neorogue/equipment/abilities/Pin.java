package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Pin extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties aoe = TargetProperties.radius(2, true, TargetType.ENEMY);
	private int damage, reduction;
	
	public Pin(boolean isUpgraded) {
		super("pin", "Pin", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 20, 0));
		damage = isUpgraded ? 150 : 100;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		TackleInstance inst = new TackleInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
	}
	
	private class TackleInstance extends EquipmentInstance {
		private HashSet<LivingEntity> hit = new HashSet<LivingEntity>();
		private boolean posX, posZ; // Which direction is the tackle going

		@SuppressWarnings("deprecation")
		public TackleInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			
			action = (pdata, in) -> {
				Util.playSound(p, Sound.ENTITY_SHULKER_SHOOT, false);
				start.spawn(p);
				Vector v = p.getEyeLocation().getDirection().setY(0).normalize().setY(0.3);
				if (p.isOnGround()) {
					p.teleport(p.getLocation().add(0, 0.2, 0));
				}
				p.setVelocity(v);
				posX = v.getX() >= 0;
				posZ = v.getZ() >= 0;
				new TackleHitChecker(p, pdata, this);
				return TriggerResult.keep();
			};
		}
		
		protected void onEnd(boolean hitWall) {
			for (LivingEntity ent : hit) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
			}
			
			if (!hitWall) return;
			Util.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, false);
			for (LivingEntity ent : hit) {
				pc.spawn(p);
				FightInstance.dealDamage(FightInstance.getFightData(ent.getUniqueId()), DamageType.BLUNT, damage, ent);
			}
		}
	}
	
	private class TackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private PlayerFightData data;
		private TackleInstance inst;
		private double vectorLen;
		
		protected TackleHitChecker(Player p, PlayerFightData data, TackleInstance inst) {
			this.data = data;
			this.inst = inst;
			this.vectorLen = p.getVelocity().lengthSquared();
			for (long delay = 1; delay <= 10; delay++) {
				final long d = delay;
				tasks.add(new BukkitRunnable() {
					public void run() {
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, aoe)) {
							inst.hit.add(ent);
						}
						
						// Check for hit wall
						if (hitWall(p)) {
							cancelTasks();
							inst.onEnd(true);
						}
						else if (d == 10) {
							inst.onEnd(false);
						}
					}
				}.runTaskLater(NeoRogue.inst(), delay));
			}
			
			data.addCleanupTask(id, () -> { cancelTasks(); });
		}
		
		private boolean hitWall(Player p) {
			double currLen = p.getVelocity().lengthSquared();
			if (vectorLen - currLen > 0.2) {
				// Check x-dir blocks
				Block xb = p.getLocation().getBlock().getRelative(inst.posX ? BlockFace.EAST : BlockFace.WEST);
				for (int i = 0; i < 2; i++) {
					if (xb.isCollidable()) return true;
					xb = xb.getRelative(BlockFace.UP);
				}
				
				// Check z-dir blocks
				Block zb = p.getLocation().getBlock().getRelative(inst.posZ ? BlockFace.NORTH : BlockFace.SOUTH);
				for (int i = 0; i < 2; i++) {
					if (zb.isCollidable()) return true;
					zb = zb.getRelative(BlockFace.UP);
				}
				
				// Check diagonal blocks
				Block diag = zb.getRelative(inst.posX ? BlockFace.EAST : BlockFace.WEST);
				for (int i = 0; i < 2; i++) {
					if (diag.isCollidable()) return true;
					diag = zb.getRelative(BlockFace.DOWN);
				}
			}
			
			vectorLen = currLen;
			return false;
		}
		
		private void cancelTasks() {
			data.removeCleanupTask(id);
			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, new String[] { "<gold>Area of Effect: <white>2" },
				"On cast, dash forward, taking all enemies you contact with you. Slow all enemies hit for <white>5</white> seconds. If you hit a wall,"
				+ " deal " + GlossaryTag.BLUNT.tag(this) + " <yellow>" + damage + "</yellow> damage and reduce the damage of all enemies hit by "
				+ reduction + " for <white>5</white> seconds.");
	}
}
