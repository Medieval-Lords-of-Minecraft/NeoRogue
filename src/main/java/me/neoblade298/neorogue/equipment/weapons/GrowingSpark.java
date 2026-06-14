package me.neoblade298.neorogue.equipment.weapons;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GrowingSpark extends Equipment {
	private static final String ID = "GrowingSpark";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK).spread(0.2, 0.2);
	private ItemStack chargedIcon;
	private int damage, growth;
	
	public GrowingSpark(boolean isUpgraded) {
		super(ID, "Growing Spark", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 3, 10));
		damage = 80;
		growth = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		ProjectileGroup proj = new ProjectileGroup(new GrowingSparkProjectile(data, inst, slot, this));
		inst.setAction((pdata, in) -> {
			data.wandDelay(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class GrowingSparkProjectile extends Projectile {
		private PlayerFightData data;
		private EquipmentInstance inst;
		private int stacks = 0;
		private long lastCast = 0;
		private Equipment eq;
		private int slot;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public GrowingSparkProjectile(PlayerFightData data, EquipmentInstance inst, int slot, Equipment eq) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.inst = inst;
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.firework.play(data.getPlayer(), data.getPlayer());
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage + growth * (stacks + 1), DamageType.LIGHTNING, DamageStatTracker.of(id + slot, eq)));
			stacks = Math.min(5, stacks + 1);
			chargedIcon.setAmount(stacks);
			inst.setIcon(chargedIcon);
			lastCast = System.currentTimeMillis();
			final long fcast = lastCast;
			data.addTask(new BukkitRunnable() {
				public void run() {
					// Check if the last cast is still the same as it was 6s ago, aka see if the skill was cast again
					if (fcast == lastCast) {
						stacks = 0;
						inst.setIcon(item);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 120));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.QUARTZ, "On cast, " + GlossaryTag.CHARGE.tag(this) + " <white>1s</white> before firing a projectile that deals "
				+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage. Every time you cast within " + DescUtil.white("6s") + " of the last cast, increase " +
				"its damage by " + DescUtil.yellow(growth) + ", up to " + DescUtil.white(5) + ". Otherwise, reset the stacks.");
		chargedIcon = item.clone().withType(Material.NETHER_STAR);
	}
}
