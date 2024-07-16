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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DoubleStrike extends Equipment {
	private static final String ID = "doubleStrike";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT_MAGIC);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK);
	
	public DoubleStrike(boolean isUpgraded) {
		super(ID, "Double Strike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 25 : 15, 5, 8, 0));
		properties.addUpgrades(PropertyType.MANA_COST);
		
		pc.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On cast, reset your basic attack cooldown.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			sc.play(p, p);
			pc.play(p, p);
			data.resetBasicAttackCooldown(EquipSlot.HOTBAR);
			data.resetBasicAttackCooldown(EquipSlot.OFFHAND);
			return TriggerResult.keep();
		}));
	}
}
