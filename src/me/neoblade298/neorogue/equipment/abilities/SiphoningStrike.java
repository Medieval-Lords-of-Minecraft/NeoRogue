package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class SiphoningStrike extends Equipment {
	private int damage, buff;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public SiphoningStrike(boolean isUpgraded) {
		super("siphoningStrike", "Siphoning Strike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 12, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = 150;
		buff = isUpgraded ? 8 : 5;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new ControlledExecuteInstance(p, this, slot, es));
	}
	
	private class ControlledExecuteInstance extends EquipmentInstance {

		@SuppressWarnings("deprecation")
		public ControlledExecuteInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, inputs) -> {
				Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
				pc.spawn(p);
				pdata.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
					if (p.isOnGround()) return TriggerResult.keep();
					BasicAttackEvent ev = (BasicAttackEvent) in;
					Util.playSound(p, Sound.BLOCK_ANVIL_LAND, 1F, 1F, false);
					hit.spawn(ev.getTarget().getLocation());
					FightInstance.dealDamage(pdata, DamageType.PIERCING, damage, ev.getTarget());
					
					if (ev.getTarget().getHealth() <= 0) {
						pdata.applyStatus(StatusType.STRENGTH, p.getUniqueId(), buff, -1);
						Util.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, false);
					}
					return TriggerResult.remove();
				});
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack while in the air deals <white>" + damage + "</white> " + GlossaryTag.PIERCING.tag(this)
				+ " damage. If the enemy is killed with this damage, increase " + GlossaryTag.STRENGTH.tag(this) + 
				" by <yellow>" + buff + "</yellow>.");
	}
}
