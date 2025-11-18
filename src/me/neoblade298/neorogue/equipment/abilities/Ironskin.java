package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Ironskin extends Equipment {
	private static final String ID = "Ironskin";
	private int shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final SoundContainer sc = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN);
	
	public Ironskin(boolean isUpgraded) {
		super(ID, "Ironskin", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, 
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 50, 10, 0));
		shields = isUpgraded ? 15 : 10;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COBBLESTONE,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (fd, in) -> {
			data.addPermanentShield(p.getUniqueId(), shields);
			sc.play(p, p);
			pc.play(p, p);
			return TriggerResult.keep();
		}));
	}
}
