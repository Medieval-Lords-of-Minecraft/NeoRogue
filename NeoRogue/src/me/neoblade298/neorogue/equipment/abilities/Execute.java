package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Execute extends Equipment {
	private int damage, execute;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public Execute(boolean isUpgraded) {
		super("execute", "Execute", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, isUpgraded ? 5 : 7, 0));
		damage = isUpgraded ? 45 : 30;
		execute = isUpgraded ? 150 : 100;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
		addReforgeOption("empoweredEdge", "recklessSwing", "blessedEdge", "fury");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, (pdata, inputs) -> {
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			pc.spawn(p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				double pct = ev.getTarget().getHealth() / ev.getTarget().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				hit.spawn(ev.getTarget().getLocation());
				if (pct < 0.5) {
					FightInstance.dealDamage(data, DamageType.PIERCING, damage + execute, ev.getTarget());
					Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
				}
				else {
					FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget());
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack while in the air deals <yellow>" + damage + " </yellow>piercing damage. If the enemy is"
						+ " below <gold>50%</gold> health, deal an additional <yellow>" + execute + "</yellow> piercing damage.");
	}
}
