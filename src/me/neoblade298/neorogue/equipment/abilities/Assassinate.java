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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Assassinate extends Equipment {
	private static final String ID = "Assassinate";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Assassinate(boolean isUpgraded) {
		super(ID, "Assassinate", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 40 : 30, 12, 0));
		properties.addUpgrades(PropertyType.STAMINA_COST);
		damage = 180;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in2) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in2;
				FightInstance.dealDamage(data, DamageType.PIERCING, damage, ev.getTarget(), DamageStatTracker.of(id + slot, this));
				hit.play(p, ev.getTarget());
				Sounds.anvil.play(p, ev.getTarget());
				if (ev.getTarget().getHealth() <= 0) {
					Sounds.success.play(p, p);
					data.addStamina(isUpgraded ? 40 : 30);
					inst.setCooldown(0);
				}
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals " + GlossaryTag.PIERCING.tag(this, damage, true) + ". If you kill an enemy,"
				+ " the stamina cost is refunded and the cooldown is reset.");
	}
}
