package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
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

public class Firewall extends Equipment {
	private static final String ID = "firewall";
	private int damage, burn;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME),
		wall = pc.clone().spread(0.2, 2).count(15).offsetY(1);
	private static final TargetProperties tp = TargetProperties.line(10, 4, true, TargetType.BOTH).canTargetSource(true);
	
	public Firewall(boolean isUpgraded) {
		super(ID, "Firewall", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 5, 22, tp.range));
		damage = isUpgraded ? 75 : 50;
		burn = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FirewallProjectile(data, this, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			proj.start(data);
			return TriggerResult.keep();
		}));
	}
	
	private class FirewallProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Equipment eq;
		private int slot;

		public FirewallProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, properties.get(PropertyType.RANGE), 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.ignore(false, false, true);
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}
		
		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location end = b.getLocation().clone();
			Location start = proj.getActionMeta().getLocation();
			end.setY(start.getY());
			activateFirewall(start, end, slot);
		}

		@Override
		public void onFizzle(ProjectileInstance proj) {
			Location end = proj.getLocation().clone();
			Location start = proj.getActionMeta().getLocation();
			end.setY(start.getY());
			activateFirewall(start, end, slot);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getActionMeta().setLocation(proj.getLocation().clone());
			Sounds.fire.play(p, p);
		}

		private void activateFirewall(Location start, Location end, int slot) {
			data.addTask(new BukkitRunnable() {
				private int tick = 0;
				public void run() {
					ParticleUtil.drawLine(p, wall, start, end, 1);
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						if (!(ent instanceof Player)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)), ent);
						}
						// Apply burn to enemies and caster
						if (ent == p || !(ent instanceof Player)) {
							FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
						}
					}

					if (++tick >= 7) {
						// Stop the task after 7 seconds
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20, 20));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RED_CONCRETE_POWDER,
				"On cast, fire a projectile that travels until it hits a block or max range. Create a wall of fire along where the projectile traveled which deals "
				+ GlossaryTag.FIRE.tag(this, damage, true) + " to enemies and applies " + GlossaryTag.BURN.tag(this, burn, true) +
				" to enemies and yourself every second for <white>7s</white>.");
	}
}
