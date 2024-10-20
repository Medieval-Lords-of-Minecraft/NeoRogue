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
		stacks = 20;
		growth = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20);
			ProjectileGroup proj = new ProjectileGroup(new InflameProjectile(data));
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

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public InflameProjectile(PlayerFightData data) {
			super(properties.get(PropertyType.RANGE), 2);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, hitBarrier);
			boolean lvlUp = hit.hasStatus(StatusType.BURN);
			hit.applyStatus(StatusType.BURN, data, stacks + (growth * lvl), -1);
			if (lvlUp) lvl++;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			DamageMeta dm = proj.getMeta();
			dm.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAMPFIRE, "On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white> before firing a projectile that deals "
				+ GlossaryTag.FIRE.tag(this, damage, true) + " damage and applies " +
				GlossaryTag.BURN.tag(this, stacks, false) + ". If the enemy hit already has " + GlossaryTag.BURN.tag(this) + ", increase the stacks applied by " +
				"this ability for the fight by " + DescUtil.yellow(growth) + ".");
	}
}
