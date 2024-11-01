package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SparkStick extends Equipment {
	private static final String ID = "sparkStick";
	private static final ParticleContainer tick;
	
	private int pierceAmount;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(3).spread(0.1, 0.1).speed(0);
	}
	
	public SparkStick(boolean isUpgraded) {
		super(
				ID, "Spark Stick", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 1 : 2, 0, 5, 4, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.MANA_COST);
		pierceAmount = 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new SparkStickProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class SparkStickProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int pierceAmount;

		public SparkStickProjectile(PlayerFightData data) {
			super(2.5, 10, 1);
			this.size(0.5, 0.5).pierce(pierceAmount);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			Sounds.explode.play(p, loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, properties);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK, "Pierces through the first <white>" + pierceAmount + "</white> enemy hit. Also grants <white>3</white> mana per enemy hit."
		);
	}
}
