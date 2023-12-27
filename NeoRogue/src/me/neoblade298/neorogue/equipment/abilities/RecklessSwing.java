package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class RecklessSwing extends Equipment {
	private int damage;
	private static int HEALTH_COST;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public RecklessSwing(boolean isUpgraded) {
		super("recklessSwing", "Reckless Swing", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 7, 0));
		damage = isUpgraded ? 600 : 400;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(this,
				(pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1F, 1F, false);
			p.setHealth(p.getHealth() - HEALTH_COST);
			pc.spawn(p);
			pdata.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				FightInstance.dealDamage(p, DamageType.SLASHING, damage, (Damageable) in2[1]);
				hit.spawn(((Damageable) in2[1]).getLocation());
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		},
				(p2, pdata) -> {
			if (p2.getHealth() <= 5) {
				Util.displayError(data.getPlayer(), "Not enough health!");
				return false;
			}
			return true;
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals <yellow>" + damage + " </yellow>damage at the cost of <yellow>" + HEALTH_COST
						+ "</yellow> health.");
	}
}
