package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaShell extends Equipment {
	private static final String ID = "ManaShell";
	private int shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public ManaShell(boolean isUpgraded) {
		super(ID, "Mana Shell", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(9, 0, 5, 0));
		shields = isUpgraded ? 6 : 4;
	}

	@Override
	public void setupReforges() {
		addReforge(ForceCloak.get(), ManaCloak.get());
		addReforge(CalculatingGaze.get(), ManaGuard.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>5s</white>.");
	}
}
