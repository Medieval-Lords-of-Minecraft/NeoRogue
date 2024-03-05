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

public class Brace extends Equipment {
	private int shields;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN);
	
	public Brace(boolean isUpgraded) {
		super("brace", "Brace", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		shields = isUpgraded ? 30 : 20;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		
		tags.add(GlossaryTag.SHIELDS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			pc.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			equip.play(p, p);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, gain <yellow>" + shields + "</yellow> " + GlossaryTag.SHIELDS.tag(this) + " for 5 seconds.");
	}
}
