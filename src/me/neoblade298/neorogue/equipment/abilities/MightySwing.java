package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class MightySwing extends Equipment {
	private static final String ID = "MightySwing";
	private int damage, cdr;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public MightySwing(boolean isUpgraded) {
		super(ID, "Mighty Swing", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 25 : 35, 10, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = 300;
		cdr = isUpgraded ? 4 : 3;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance eqi = new EquipmentInstance(data, this, slot, es);
		data.addTrigger(id, bind, eqi);
		eqi.setAction((pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				if (p.isOnGround()) return TriggerResult.keep();
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				double pct = ev.getTarget().getHealth() / ev.getTarget().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				Sounds.anvil.play(p, p);
				hit.play(p, ev.getTarget().getLocation());
				if (pct > 0.5) {
					eqi.reduceCooldown(cdr);
					Sounds.fire.play(p, p);
				}
				else {
					FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, this));
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RED_DYE,
				"On cast, your next basic attack while in the air deals <yellow>" + damage + "</yellow> " + GlossaryTag.PIERCING.tag(this) + " damage. If the enemy is"
						+ " above <white>50%</white> health, reduce the ability's cooldown by <yellow>" + cdr + "</yellow>.");
	}
}
