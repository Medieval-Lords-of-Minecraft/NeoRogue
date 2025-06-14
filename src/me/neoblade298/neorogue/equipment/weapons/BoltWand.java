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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BoltWand extends Equipment {
	private static final String ID = "boltWand";
	private static final ParticleContainer tick;
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);

	private int pierceAmount, elec;
	
	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(3).spread(0.1, 0.1).speed(0);
	}
	
	public BoltWand(boolean isUpgraded) {
		super(
				ID, "Bolt Wand", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(2, 0, isUpgraded ? 45 : 35, 0.8, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		pierceAmount = 3;
		elec = 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new BoltWandProjectile(p, data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class BoltWandProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;

		public BoltWandProjectile(Player p, PlayerFightData data) {
			super(2.5, 12, 1);
			this.size(0.5, 0.5).pierce(pierceAmount);
			this.p = p;
			this.data = data;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play((Player) proj.getOwner().getEntity(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			hit.applyStatus(StatusType.ELECTRIFIED, proj.getOwner(), 5, -1);
			Location loc = hit.getEntity().getLocation();
			sc.play(p, loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, properties);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK, "Pierces the first <white>" + pierceAmount + "</white> enemies hit, and applies " + GlossaryTag.ELECTRIFIED.tag(this, elec, false) + " to all."
		);
	}
}
