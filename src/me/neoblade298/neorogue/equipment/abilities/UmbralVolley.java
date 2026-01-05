package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class UmbralVolley extends Equipment {
	private static final String ID = "UmbralVolley";
	private static final int COOLDOWN = 60; // 3 seconds in ticks
	private int damage;
	private ProjectileGroup projs = new ProjectileGroup();
	private static final ParticleContainer part = new ParticleContainer(Particle.SQUID_INK)
			.count(3).spread(0.3, 0.3);
	
	public UmbralVolley(boolean isUpgraded) {
		super(ID, "Umbral Volley", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 4));
		
		damage = isUpgraded ? 80 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		for (int i = 0; i < 5; i++) {
			projs.add(new UmbralVolleyProjectile(i, slot, this));
		}
		
		ActionMeta cooldown = new ActionMeta();
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (cooldown.getTime() >= System.currentTimeMillis()) return TriggerResult.keep();
			
			cooldown.setTime(System.currentTimeMillis() + COOLDOWN * 50); // Convert ticks to ms
			Sounds.attackSweep.play(p, p);
			projs.start(data);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"Passive. On a <white>3s</white> cooldown, on basic attack, fire <white>5</white> dark needles in a cone in front of you that deal "
						+ GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
	
	private class UmbralVolleyProjectile extends Projectile {
		private int slot;
		private Equipment eq;
		public UmbralVolleyProjectile(int i, int slot, Equipment eq) {
			super(1, properties.get(PropertyType.RANGE), 1);
			int iter = i - 2;
			this.rotation(iter * 15);
			this.size(1, 1);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = (Player) proj.getOwner().getEntity();
			Sounds.attackSweep.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.DARK, DamageStatTracker.of(ID + slot, eq)));
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			part.play(p, proj.getLocation());
		}
	}
}
