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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Discipline extends Equipment {
	private static final String ID = "discipline";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final int stamina = 50;
	private int staminaGain;
	
	public Discipline(boolean isUpgraded) {
		super(ID, "Discipline", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 25, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		staminaGain = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <white>" + stamina + "</white> stamina, <yellow>" + staminaGain + "</yellow> max stamina, and"
						+ " take <white>7</white> less damage for <white>10</white> seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			pdata.addMaxStamina(staminaGain);
			pdata.addStamina(stamina);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, 7, 0), 200);
			return TriggerResult.keep();
		}));
	}
}
