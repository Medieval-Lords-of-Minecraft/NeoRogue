package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class Brace2 extends Ability {
	private int shields;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public Brace2(boolean isUpgraded) {
		super("brace2", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		display = "Brace II";
		canDrop = false;
		setBaseProperties(20, 0, 100, 0);
		shields = isUpgraded ? 180 : 120;
		item = createItem(this, Material.FLINT, null,
				"On cast, gain <yellow>" + shields + " </yellow>shields for 5 seconds.");
		pc.count(10).offset(0.5, 0.5).speed(0.2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, bind, (fd, in) -> {
			pc.spawn(p);
			data.addShield(p.getUniqueId(), shields, true, 100, 100, 0, 1);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			return true;
		});
	}
}
