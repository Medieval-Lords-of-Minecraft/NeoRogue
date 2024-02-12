package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Discipline extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private static final int stamina = 50;
	private int staminaGain;
	
	public Discipline(boolean isUpgraded) {
		super("discipline", "Discipline", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 15, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		staminaGain = isUpgraded ? 15 : 10;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <white>" + stamina + " </white> stamina and <yellow>" + staminaGain + "</yellow> max stamina.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			pc.spawn(p);
			pdata.addMaxStamina(staminaGain);
			pdata.addStamina(stamina);
			return TriggerResult.keep();
		}));
	}
}
