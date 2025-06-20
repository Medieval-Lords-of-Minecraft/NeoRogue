package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.Effect;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
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
	private static final String ID = "chainLightningWand";
	private static final TargetProperties hitScan = TargetProperties.cone(9, 10, false, TargetType.ENEMY);
	private static final TargetProperties chainScan = TargetProperties.cone(60, 5, false, TargetType.ENEMY);

	private static final ParticleContainer tick;
	private LinkedList<Player> drawCache;

	private int chainAmount;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(2).spread(0.1, 0.1).speed(0);
	}

	public ChainLightningWand(boolean isUpgraded) {
		super(
				ID, "Chain Lightning Wand", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, isUpgraded ? 70 : 50, 0.8, DamageType.LIGHTNING, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		chainAmount = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			LivingEntity target = TargetHelper.getNearestInSight(p, hitScan);
			if (target != null) {
				weaponSwing(p, data);
				weaponDamage(p, data, target);
				drawCache = Effect.calculateCache(p, p.getLocation(), Audience.NONE, ParticleContainer.HIDE_TAG);
				drawChains(p.getLocation(), target.getLocation());
				chainHit(p, data, target, chainAmount);
			}

			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Lightning chains through <yellow>" + chainAmount + "</yellow> additional enemies.");
	}

	// returns chains remaining
	private int chainHit(Player p, PlayerFightData data, LivingEntity chainSource, int chainsRemaining) {
		if (chainsRemaining <= 0)
			return 0;
		
		LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInCone(chainSource, p.getLocation().getDirection(), chainScan);
		
		for (LivingEntity target : targets) {
			drawChains(chainSource.getLocation(), target.getLocation());
			weaponDamage(p, data, target);
			chainsRemaining--;
			if (chainsRemaining <= 0)
				break;
		}

		for (LivingEntity target : targets) {
			chainsRemaining = chainHit(p, data, target, chainsRemaining);
			if (chainsRemaining <= 0)
				break;
		}
		
		return chainsRemaining;
	}

	private void drawChains(Location pos1, Location pos2) {
		ParticleUtil.drawLineWithCache(drawCache, tick, pos1, pos2, 0.4);
	}
}
