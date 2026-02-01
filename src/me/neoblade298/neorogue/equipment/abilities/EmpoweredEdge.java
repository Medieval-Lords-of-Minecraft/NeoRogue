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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EmpoweredEdge extends Equipment {
	private static final String ID = "EmpoweredEdge";
	private int damage, shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public EmpoweredEdge(boolean isUpgraded) {
		super(ID, "Empowered Edge", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, isUpgraded ? 5 : 7, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = isUpgraded ? 70 : 50;
		shields = isUpgraded ? 4 : 3;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}

	@Override
	public void setupReforges() {
		addReforge(BasicInfusionMastery.get(), BlessedEdge.get());
		addReforge(Furor.get(), Fury.get());
		addReforge(EnduranceTraining.get(), Embolden.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.SLASHING,
						DamageStatTracker.of(id + slot, this)));
				hit.play(p, ev.getTarget());
				Sounds.anvil.play(p, ev.getTarget());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, grant yourself " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>5</white> seconds. "
						+ "Your next basic attack deals " + GlossaryTag.SLASHING.tag(this, damage, true) + " damage.");
	}
}
