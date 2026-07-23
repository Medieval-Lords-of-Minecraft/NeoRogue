package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class AbyssalCarve extends Equipment implements Power {
	private static final String ID = "AbyssalCarve";
	private static final ParticleContainer slash = new ParticleContainer(Particle.SWEEP_ATTACK);
	private static final ParticleContainer trail = new ParticleContainer(Particle.SOUL);
	private int damage;
	private ProjectileGroup projs;
	
	public AbyssalCarve(boolean isUpgraded) {
		super(ID, "Abyssal Carve", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 250 : 175;
		slash.count(3).spread(0, 0).speed(0);
		trail.count(15).spread(0.3, 0.3).speed(0.02);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		projs = new ProjectileGroup(new AbyssalSlashProjectile(slot, this));
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.EVADE, (pdata2, in2) -> {
					Player p = data.getPlayer();
					EvadeEvent ev = (EvadeEvent) in2;
					
					// Get the damager entity from the DamageMeta
					if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
						return TriggerResult.keep();
					}
					
					LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
					Location playerLoc = p.getLocation();
					Location damagerLoc = damager.getLocation();

					// Calculate direction toward the attacker
					Vector towardEnemy = damagerLoc.toVector().subtract(playerLoc.toVector()).normalize();
					
					// Dash away from the enemy
					Vector awayFromEnemy = towardEnemy.clone().multiply(-1);
					data.dash(awayFromEnemy);
					
					carve(data, p, towardEnemy);
					return TriggerResult.keep();
				});
				data.addTrigger(id + "-active-dash", Trigger.DASH, (pdata2, in2) -> {
					Player p = data.getPlayer();
					Vector dir = p.getLocation().getDirection();
					carve(data, p, dir);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	private void carve(PlayerFightData data, Player p, Vector direction) {
		Sounds.flap.play(p, p);
		slash.play(p, p);
		trail.play(p, p);
		projs.start(data, p.getLocation().add(0, p.isSneaking() ? 1.0 : 1.4, 0), direction.clone());
		Sounds.attackSweep.play(p, p);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after evading once. Upon " + GlossaryTag.EVADE.tag(this) + " or " + GlossaryTag.DASH.tag(this) + ", launch a slash projectile toward the attacker that deals " + 
				GlossaryTag.DARK.tag(this, damage) + " damage and pierces. On " + GlossaryTag.EVADE.tag(this) + ", also " + 
				GlossaryTag.DASH.tag(this) + " away from them.");
	}
	
	private class AbyssalSlashProjectile extends Projectile {
		private Equipment eq;
		private int slot;
		
		public AbyssalSlashProjectile(int slot, Equipment eq) {
			super(0.5, 8, 2);
			this.size(1.5, 1.5);
			this.pierce(-1);
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			slash.play(p, proj.getLocation());
			trail.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.DARK, DamageStatTracker.of(id + slot, eq)));
		}
	}
}
