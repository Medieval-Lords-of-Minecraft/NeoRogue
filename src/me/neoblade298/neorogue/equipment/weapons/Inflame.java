package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Inflame extends Equipment {
	private static final String ID = "inflame";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int damage, stacks, growth;
	
	public Inflame(boolean isUpgraded) {
		super(ID, "Inflame", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 5, 13, 8));
		damage = isUpgraded ? 140 : 100;
		stacks = 50;
		growth = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new InflameProjectile(data, slot, this));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		}));
	}
	
	private class InflameProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private int lvl = 0;
		private int slot;
		private Equipment eq;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public InflameProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.p = data.getPlayer();
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			boolean lvlUp = hit.hasStatus(StatusType.BURN);
			hit.applyStatus(StatusType.BURN, data, stacks + (growth * lvl), -1);
			if (lvlUp) lvl++;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAMPFIRE, "On cast, " + DescUtil.charge(this, 1, 1) + " before firing a projectile that deals "
				+ GlossaryTag.FIRE.tag(this, damage, true) + " damage and applies " +
				GlossaryTag.BURN.tag(this, stacks, false) + ". If the enemy hit already has " + GlossaryTag.BURN.tag(this) + ", increase the stacks applied by " +
				"this ability for the fight by " + DescUtil.yellow(growth) + ".");
	}
}
