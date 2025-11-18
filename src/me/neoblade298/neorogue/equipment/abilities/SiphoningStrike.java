package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class SiphoningStrike extends Equipment {
	private static final String ID = "SiphoningStrike";
	private int damage, buff;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public SiphoningStrike(boolean isUpgraded) {
		super(ID, "Siphoning Strike", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 12, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = 180;
		buff = isUpgraded ? 25 : 15;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		inst.setAction((pdata, inputs) -> {
				Sounds.equip.play(p, p);
				pc.play(p, p);
				pdata.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
					if (p.isOnGround()) return TriggerResult.keep();
					PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
					Sounds.anvil.play(p, p);
					hit.play(p, ev.getTarget().getLocation());
					FightInstance.dealDamage(pdata, DamageType.PIERCING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, this));
					
					if (ev.getTarget().getHealth() <= 0) {
						pdata.applyStatus(StatusType.STRENGTH, pdata, buff, -1);
						Sounds.success.play(p, p);
						inst.reduceCooldown(6);
					}
					return TriggerResult.remove();
				});
				return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BROWN_DYE,
				"On cast, your next basic attack while in the air deals <white>" + damage + "</white> " + GlossaryTag.PIERCING.tag(this)
				+ " damage. If the enemy is killed with this damage, increase " + GlossaryTag.STRENGTH.tag(this) + 
				" by <yellow>" + buff + "</yellow> and halve the ability cooldown.");
	}
}
