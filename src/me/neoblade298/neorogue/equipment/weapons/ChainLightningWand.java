package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ChainLightningWand extends Equipment {
	private static final TargetProperties hitScan = TargetProperties.cone(12, 10, false, TargetType.ENEMY);
	
	private static ParticleContainer tick;
	
	private int chainAmount;
	
	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(3).spread(0.1, 0.1).speed(0.01);
	}
	
	public ChainLightningWand(boolean isUpgraded) {
		super(
				"chainLightningWand", "Chain Lightning Wand", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(10, 0, isUpgraded ? 40 : 30, 0.5, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		chainAmount = isUpgraded ? 5 : 3;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			LivingEntity target = TargetHelper.getEntitiesInCone(p, hitScan).pollFirst();
			if (target != null) {
				// TODO: chain hits
			}
			
			weaponSwing(p, data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Lightning chain hits through <yellow>" + chainAmount + "</yellow> enemies.");
	}
}
