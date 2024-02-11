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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Consume extends Equipment {
	private int damage, execute, heal;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE),
			healPart = new ParticleContainer(Particle.VILLAGER_HAPPY);
	
	public Consume(boolean isUpgraded) {
		super("consume", "Consume", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, isUpgraded ? 5 : 7, 0));
		damage = 45;
		execute = 150;
		heal = isUpgraded ? 6 : 4;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
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
				
				if (ev.getTarget().getHealth() <= 0) {
					data.addHealth(heal);
					Util.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, false);
					healPart.spawn(p);
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack while in the air deals <white>" + damage + "</white> " + GlossaryTag.PIERCING.tag(this)
				+ " damage. If the enemy is below <white>50%</white> health, deal an additional <white>" + execute + "</white> " +
				GlossaryTag.PIERCING.tag(this) + " damage. If the enemy is killed with this damage, heal for <yellow>" + heal + "</yellow>.");
	}
}
