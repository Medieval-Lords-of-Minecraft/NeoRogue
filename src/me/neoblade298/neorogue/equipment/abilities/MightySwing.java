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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class MightySwing extends Equipment {
	private int damage, cdr;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public MightySwing(boolean isUpgraded) {
		super("mightySwing", "Mighty Swing", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 25 : 35, 10, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = 130;
		cdr = isUpgraded ? 4 : 3;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance eqi = new EquipmentInstance(p, this, slot, es);
		data.addTrigger(id, bind, eqi);
		eqi.setAction((pdata, inputs) -> {
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			pc.spawn(p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
				if (p.isOnGround()) return TriggerResult.keep();
				BasicAttackEvent ev = (BasicAttackEvent) in;
				double pct = ev.getTarget().getHealth() / ev.getTarget().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
				hit.spawn(ev.getTarget().getLocation());
				if (pct > 0.5) {
					eqi.reduceCooldown(cdr);
					Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
				}
				else {
					FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget());
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack while in the air deals <yellow>" + damage + "</yellow> " + GlossaryTag.PIERCING.tag(this) + " damage. If the enemy is"
						+ " above <white>50%</white> health, reduce the ability's cooldown by <yellow>" + cdr + "</yellow>.");
	}
}
