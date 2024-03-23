package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Envenom extends Equipment {
	private static final String ID = "envenom";
	private int poison;
	private static final ParticleContainer pc = new ParticleContainer(Particle.REDSTONE).dustOptions(new DustOptions(Color.GREEN, 1));
	
	public Envenom(boolean isUpgraded) {
		super(ID, "Envenom", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, isUpgraded ? 5 : 7, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		poison = isUpgraded ? 3 : 2;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Embolden.get(), Fury.get(), BlessedEdge.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			data.addSimpleShield(p.getUniqueId(), 1, 100L);
			pc.play(p, p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				FightInstance.dealDamage(data, DamageType.SLASHING, 1, ev.getTarget());
				Sounds.anvil.play(p, ev.getTarget());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, grant yourself " + GlossaryTag.SHIELDS.tag(this, 1, true) + " for <white>5</white> seconds. "
						+ "Your next basic attack deals " + GlossaryTag.PIERCING.tag(this, 1, true) + ".");
	}
}
