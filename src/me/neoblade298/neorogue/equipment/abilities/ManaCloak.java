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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaCloak extends Equipment {
	private static final String ID = "manaCloak";
	private int shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public ManaCloak(boolean isUpgraded) {
		super(ID, "Mana Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 30 : 60, 0, 10, 0));
				properties.addUpgrades(PropertyType.MANA_COST);
		shields = 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 140);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>10s</white>, " + GlossaryTag.PROTECT.tag(this, 1, false) + 
				", and " + GlossaryTag.SHELL.tag(this, 1, false) + ".");
	}
}
