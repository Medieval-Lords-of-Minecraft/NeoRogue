package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Pin extends Equipment {
	private static final String ID = "pin";
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties aoe = TargetProperties.radius(2, true, TargetType.ENEMY);
	private int damage, reduction;
	
	public Pin(boolean isUpgraded) {
		super(ID, "Pin", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 15, 0, 2));
		damage = isUpgraded ? 160 : 130;
		reduction = isUpgraded ? 15 : 10;
		
		pc.count(25).spread(1, 1);
		start.count(25).spread(0.5, 0);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		TackleInstance inst = new TackleInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
	}
	
	private class TackleInstance extends EquipmentInstance {
		private HashSet<LivingEntity> hit = new HashSet<LivingEntity>();
		private boolean posX, posZ; // Which direction is the tackle going

		@SuppressWarnings("deprecation")
		public TackleInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			
			action = (pdata, in) -> {
				Sounds.jump.play(p, p);
				start.play(p, p);
				Vector v = p.getEyeLocation().getDirection().setY(0).normalize().multiply(1.2).setY(0.3);
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
		
		protected void onEnd(PlayerFightData data, boolean hitWall) {
			data.removeCleanupTask(id);
			for (LivingEntity ent : hit) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
			}
			
			if (!hitWall) {
				hit.clear();
				return;
			}
			
			Sounds.explode.play(p, p);
			pc.play(p, p);
			for (LivingEntity ent : hit) {
				FightInstance.dealDamage(FightInstance.getFightData(ent.getUniqueId()), DamageType.BLUNT, damage, ent, DamageStatTracker.of(id + slot, eq));
			}
			hit.clear();
		}
	}
	
	private class TackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private TackleInstance inst;
		
		protected TackleHitChecker(Player p, PlayerFightData data, TackleInstance inst) {
			this.inst = inst;
			for (long delay = 1; delay <= 12; delay++) {
				final long d = delay;
				tasks.add(new BukkitRunnable() {
					public void run() {
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, aoe)) {
							inst.hit.add(ent);
						}
						
						for (LivingEntity ent : inst.hit) {
							ent.teleport(p);
						}
						
						if (d < 5) return;
						
						// Check for hit wall only after the first few ticks
						if (hitWall(p)) {
							cancelTasks();
							inst.onEnd(data, true);
						}
						else if (d == 10) {
							inst.onEnd(data, false);
						}
					}
				}.runTaskLater(NeoRogue.inst(), delay));
			}
			
			data.addCleanupTask(id, () -> { cancelTasks(); });
		}
		
		private boolean hitWall(Player p) {
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
			
			return false;
		}
		
		private void cancelTasks() {
			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, dash forward, taking all enemies you contact with you. Slow all enemies hit for <white>5</white> seconds. If you hit a wall,"
				+ " deal " + GlossaryTag.BLUNT.tag(this) + " <yellow>" + damage + "</yellow> damage and reduce the damage of all enemies hit by <white>"
				+ reduction + "</white> for <white>5</white> seconds.");
	}
}
