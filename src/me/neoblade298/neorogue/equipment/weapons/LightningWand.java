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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LightningWand extends Equipment {
	private static final String ID = "lightningWand";
	private static final ParticleContainer tick;
	
	private int pierceAmount;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(3).spread(0.1, 0.1).speed(0);
	}

	public LightningWand(boolean isUpgraded) {
		super(
				ID, "Lightning Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(5, 0, isUpgraded ? 30 : 20, 0.45, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		pierceAmount = isUpgraded ? 3 : 1;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(SparkStick.get(), ChainLightningWand.get(), BoltWand.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new LightningWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	private class LightningWandProjectile extends Projectile {
		private Player p;
		
		public LightningWandProjectile(Player p) {
			super(2.5, 12, 1);
			this.size(0.5, 0.5).pierce(pierceAmount);
			this.p = p;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
			Location loc = hit.getEntity().getLocation();
			Sounds.explode.play(p, loc);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Pierces the first <yellow>" + pierceAmount + "</yellow> enemies hit.");
	}
}
