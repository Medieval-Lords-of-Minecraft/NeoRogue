package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.CalculatingGaze;
import me.neoblade298.neorogue.equipment.abilities.Manabending;
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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkBolt extends Equipment {
	private static final String ID = "darkBolt";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(Color.BLACK, 1F));
	private int damage;
	
	public DarkBolt(boolean isUpgraded) {
		super(ID, "Dark Bolt", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, isUpgraded ? 16 : 12, 10));
		damage = isUpgraded ? 210 : 140;
		properties.addUpgrades(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(CalculatingGaze.get(), Depletion.get());
		addReforge(Manabending.get(), DarkBolt2.get(), DarkTorrent.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		ProjectileGroup proj = new ProjectileGroup(new DarkBoltProjectile(data));
		inst.setAction((pdata, in) -> {
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class DarkBoltProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public DarkBoltProjectile(PlayerFightData data) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			data.addRift(new Rift(data, hit.getEntity().getLocation(), 200));
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COAL, "On cast, charge <white>1s</white> before firing a projectile that deals "
				+ GlossaryTag.DARK.tag(this, damage, true) + " damage. If you hit an enemy, create a " + GlossaryTag.RIFT.tag(this) + " [<white>10s</white>] at their location.");
	}
}
