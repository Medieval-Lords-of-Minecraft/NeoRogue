package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class Hawkeye extends Equipment implements Power {
	private static final String ID = "Hawkeye";
	private int threshold, damage;
	private static final int DAMAGE_REDUCTION = 30;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT)
		.count(3).spread(0.1, 0.1);
	
	public Hawkeye(boolean isUpgraded) {
		super(ID, "Hawkeye", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		threshold = isUpgraded ? 8 : 12;
		damage = isUpgraded ? 425 : 250;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FOCUS)) return TriggerResult.keep();
			if (count.addCount(ev.getStacks()) < 6) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
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
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata2, in2) -> {
			ReceiveDamageEvent rev = (ReceiveDamageEvent) in2;
			int focusStacks = data.getStatus(StatusType.FOCUS).getStacks();
			if (focusStacks <= threshold) return TriggerResult.keep();
			Player p2 = data.getPlayer();
			rev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, DAMAGE_REDUCTION, BuffStatTracker.defenseBuffAlly(id, this)));
			data.applyStatus(StatusType.FOCUS, data, -1, -1);
			if (rev.getDamager() != null) {
				LivingEntity damager = rev.getDamager().getEntity();
				Location playerLoc = p2.getLocation().add(0, 1, 0);
				Location damagerLoc = damager.getEyeLocation();
				Vector direction = damagerLoc.toVector().subtract(playerLoc.toVector()).normalize();
				HawkeyeProjectile proj = new HawkeyeProjectile(data, this, slot);
				proj.start(data, playerLoc, direction);
				Sounds.fire.play(p2, p2);
				pc.play(p2, playerLoc);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving " + DescUtil.white(6) + " " + GlossaryTag.FOCUS.tag(this) + " stacks. While above " + DescUtil.yellow(threshold) + " " + GlossaryTag.FOCUS.tag(this) + ", " +
				"damage taken gets reduced by " + DescUtil.white(DAMAGE_REDUCTION) + ", " +
				"reduces your " + GlossaryTag.FOCUS.tag(this) + " by " + DescUtil.white(1) + ", " +
				"and shoots a piercing projectile at the damager that deals " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
}
