package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Hellfire extends Equipment {
	private static final String ID = "Hellfire";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	
	public Hellfire(boolean isUpgraded) {
		super(ID, "Hellfire", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta projCount = new ActionMeta();
		ActionMeta burnCount = new ActionMeta();
		
		// Track projectile damage dealt
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) {
				projCount.addCount(1);
			}
			return TriggerResult.keep();
		});
		
		// Track burn applications
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.isStatus(StatusType.BURN)) {
				burnCount.addCount(1);
			}
			return TriggerResult.keep();
		});
		
		// Poll both conditions
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (projCount.getCount() < 5 || burnCount.getCount() < 3) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));
			
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				ProjectileGroup group = new ProjectileGroup(new HellfireProjectile(data, this, slot));
				LivingEntity target = ev.getTarget();
				if (target == null) return TriggerResult.keep();
				FightData fd = FightInstance.getFightData(target);
				if (fd.hasStatus(StatusType.BURN)) {
					data.addExtraShot(group);
				}
				return TriggerResult.keep();
			});
			
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				GlossaryTag.POWER.tag(this) + ". Activates after firing " + DescUtil.white(5) + " projectiles and applying " + GlossaryTag.BURN.tag(this) + " " + DescUtil.white(3) + " times. Dealing basic attack damage to an enemy with " + GlossaryTag.BURN.tag(this) + 
				" causes your next basic attack to fire an additional projectile that deals " + 
				GlossaryTag.FIRE.tag(this, damage, true) + " damage.");
	}

	private class HellfireProjectile extends Projectile {
		private AmmunitionInstance ammo;
		private PlayerFightData data;
		private Equipment eq;
		private int slot;
		
		public HellfireProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(12, 1);
			this.blocksPerTick(3);
			this.homing(0.02);
			this.data = data;
			ammo = data.getAmmoInstance();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			pc.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = data.getPlayer();
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)));
			dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, dmg, BuffStatTracker.arrowBuff(ammo.getAmmo())));
			ammo.onStart(proj);
			Sounds.fire.play(p, p);
		}
	}
}
