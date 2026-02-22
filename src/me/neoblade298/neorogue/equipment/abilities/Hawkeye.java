package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class Hawkeye extends Equipment {
	private static final String ID = "Hawkeye";
	private int threshold, damage;
	private static final int DAMAGE_REDUCTION = 30;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT)
		.count(10).spread(0.3, 0.3);
	
	public Hawkeye(boolean isUpgraded) {
		super(ID, "Hawkeye", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		threshold = isUpgraded ? 8 : 12;
		damage = isUpgraded ? 500 : 300;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			
			// Check if focus is above threshold
			int focusStacks = data.getStatus(StatusType.FOCUS).getStacks();
			if (focusStacks <= threshold) return TriggerResult.keep();
			
			Player p = data.getPlayer();
			
			// Reduce damage
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, DAMAGE_REDUCTION, null));
			
			// Reduce focus by 1
			data.applyStatus(StatusType.FOCUS, data, -1, 0);
			
			// Check if damager exists and shoot projectile
			if (ev.getDamager() != null) {
				LivingEntity damager = ev.getDamager().getEntity();
				Location playerLoc = p.getLocation().add(0, 1, 0);
				Location damagerLoc = damager.getEyeLocation();
				
				// Calculate direction from player to damager
				Vector direction = damagerLoc.toVector().subtract(playerLoc.toVector()).normalize();
				
				// Shoot projectile
				HawkeyeProjectile proj = new HawkeyeProjectile(data, this, slot);
				proj.start(data, playerLoc, direction);
				
				Sounds.shoot.play(p, p);
				pc.play(p, playerLoc);
			}
			
			return TriggerResult.keep();
		});
	}
	
	private class HawkeyeProjectile extends Projectile {
		private PlayerFightData data;
		private Equipment eq;
		private int slot;

		public HawkeyeProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1.5, 20, 1); // Speed 1.5, range 20, pierces 1 enemy
			this.data = data;
			this.eq = eq;
			this.slot = slot;
			this.pierce(1); // Piercing projectile
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Damage is added in onStart, nothing additional needed here
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, 
				DamageStatTracker.of(ID + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Passive. While above " + DescUtil.yellow(threshold) + " " + GlossaryTag.FOCUS.tag(this) + ", " +
				"taking damage gets reduced by " + DescUtil.white(DAMAGE_REDUCTION) + ", " +
				"reduces your " + GlossaryTag.FOCUS.tag(this) + " by <white>1</white>, " +
				"and shoots a piercing projectile at the damager that deals " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
}
