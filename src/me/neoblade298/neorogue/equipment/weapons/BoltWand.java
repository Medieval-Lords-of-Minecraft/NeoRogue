	package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BoltWand extends Equipment {
	private static final ParticleContainer tick;
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);

	private int pierceAmount;
	
	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(3).spread(0.1, 0.1).speed(0);
	}
	
	public BoltWand(boolean isUpgraded) {
		super(
				"boltWand", "Bolt Wand", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(5, 0, isUpgraded ? 30 : 20, 0.35, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		pierceAmount = 3;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new BoltWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class BoltWandProjectile extends Projectile {
		private Player p;

		public BoltWandProjectile(Player p) {
			super(2.5, 12, 1);
			this.size(0.5, 0.5).pierce();
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.play((Player) proj.getOwner().getEntity(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
			hit.applyStatus(StatusType.ELECTRIFIED, p.getUniqueId(), 5, 0);
			Location loc = hit.getEntity().getLocation();
			sc.play(p, loc);
			if (proj.getNumHit() >= pierceAmount)
				proj.cancel();
		}

		@Override
		public void onStart(ProjectileInstance proj) {

		}
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK, "Pierces the first <white>3</white> enemies hit, and applies <white>5</white> " + GlossaryTag.ELECTRIFIED.tag(this) + " to all."
		);
	}
}
