package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PhantasmalKiller extends Equipment {
	private static final String ID = "PhantasmalKiller";
	private static final int MAX_CHARGES = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.SOUL).count(25).spread(0.5, 0.5).offsetY(1);
	private static final ParticleContainer tick = new ParticleContainer(Particle.SOUL).count(1);
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	
	public PhantasmalKiller(boolean isUpgraded) {
		super(ID, "Phantasmal Killer", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(isUpgraded ? 90 : 75, 1.2, 0, 6, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Nightmare.get(), ShadowyDagger.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		if (data.getSessionData().getEquipment(EquipSlot.OFFHAND)[0] != null) {
			Util.msg(p, hoverable.append(Component.text("  couldn't be equipped as you have equipment in your offhand!", NamedTextColor.RED)));
			p.getInventory().setItem(slot, null);
			return;
		}
		
		ActionMeta attacks = new ActionMeta();
		ActionMeta charges = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.NETHERITE_SWORD);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Create projectile for basic attacks
		ProjectileGroup proj = new ProjectileGroup(new PhantasmalKillerProjectile(data, this, slot, attacks, charges, inst, icon, activeIcon));
		
		// Throwable basic attacks
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			if (!canUseWeapon(data)) return TriggerResult.keep();
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Store the last basic attack's DamageMeta
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().isSecondary() && ev.getMeta().getWeapon() == this) {
				attacks.setObject(ev.getMeta().clone());
			}
			return TriggerResult.keep();
		});
		
		// Right click to consume charge and deal cloned damage
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK, (pdata, inputs) -> {
			if (charges.getCount() <= 0) return TriggerResult.keep();
			if (attacks.getObject() == null) return TriggerResult.keep();
			
			// Consume a charge
			charges.addCount(-1);
			
			// Update icon
			if (charges.getCount() > 0) {
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(charges.getCount());
				inst.setIcon(currentIcon);
			} else {
				inst.setIcon(icon);
			}
			
			// Dash forward
			data.dash(p.getEyeLocation().getDirection());
			
			// Clone the last basic attack damage
			DamageMeta dm = ((DamageMeta) attacks.getObject()).clone();
			dm.isSecondary(true);
            dm.isBasicAttack(null, false);
			
			for (DamageSlice slice : dm.getSlices()) {
				slice.setTracker(DamageStatTracker.of(id + slot, this));
			}
			
			// Deal the cloned damage to the nearest enemy
			var nearest = TargetHelper.getNearest(p, tp);
			if (nearest != null) {
				FightInstance.dealDamage(dm, nearest);
			}
			
			Sounds.attackSweep.play(p, p);
			pc.play(p, p);
			
			return TriggerResult.keep();
		});
	}
	
	private class PhantasmalKillerProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private PhantasmalKiller eq;
		private int slot;
		private ActionMeta attacks;
		private ActionMeta charges;
		private EquipmentInstance inst;
		private ItemStack activeIcon;

		public PhantasmalKillerProjectile(PlayerFightData data, PhantasmalKiller eq, int slot,
				ActionMeta attacks, ActionMeta charges, EquipmentInstance inst, ItemStack icon, ItemStack activeIcon) {
			super(0.5, 6, 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.eq = eq;
			this.p = data.getPlayer();
			this.slot = slot;
			this.attacks = attacks;
			this.charges = charges;
			this.inst = inst;
			this.activeIcon = activeIcon;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Count basic attacks
			attacks.addCount(1);
			
			// Every 3 hits, gain a charge
			if (attacks.getCount() >= 3 && charges.getCount() < MAX_CHARGES) {
				attacks.setCount(0);
				charges.addCount(1);
				
				// Update icon
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(charges.getCount());
				inst.setIcon(currentIcon);
				
				Sounds.success.play(p, p);
				pc.play(p, p);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD,
				"Throwable. Can only be used without an offhand. Every <white>3</white> basic attacks, gain a charge, up to a max of " +
				"<white>3</white>. Right click to consume a charge, dashing and dealing a clone of your " +
				"previous basic attack as damage to the nearest enemy, buffs included but not reapplied.");
	}
}
