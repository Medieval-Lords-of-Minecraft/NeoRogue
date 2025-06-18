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
		wall = pc.clone().spread(0.1, 2).count(10).speed(0.1).offsetY(1);
	private static final TargetProperties tp = TargetProperties.line(10, 2, TargetType.BOTH);
	
	public Firewall(boolean isUpgraded) {
		super(ID, "Firewall", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 5, 22, tp.range));
		damage = isUpgraded ? 60 : 40;
		burn = isUpgraded ? 20 : 10;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FirewallProjectile(data));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			proj.start(data);
			return TriggerResult.keep();
		}));
	}
	
	private class FirewallProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Location start, end;

		public FirewallProjectile(PlayerFightData data) {
			super(1, properties.get(PropertyType.RANGE), 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.ignore(false, false, true);
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
			end = b.getLocation().clone();
			end.setY(start.getY());
			activateFirewall();
		}

		@Override
		public void onFizzle(ProjectileInstance proj) {
			end = proj.getLocation();
			end.setY(start.getY());
			activateFirewall();
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			start = proj.getLocation();
			Sounds.fire.play(p, start);
		}

		private void activateFirewall() {
			data.addTask(new BukkitRunnable() {
				private int tick = 0;
				public void run() {
					ParticleUtil.drawLine(p, wall, start, end, 1);
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						if (!(ent instanceof Player)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
						}
						// Apply burn to enemies and caster
						if (ent == p || !(ent instanceof Player)) {
							FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
						}
					}

					if (++tick >= 5) {
						// Stop the task after 5 seconds
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
				" to enemies and yourself every second for <white>5s</white> .");
	}
}
