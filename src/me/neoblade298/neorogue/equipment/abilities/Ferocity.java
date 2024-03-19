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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Ferocity extends Equipment {
	private static final String ID = "ferocity";
	private static final ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private int staminaGain, cutoff, berserk;
	
	public Ferocity(boolean isUpgraded) {
		super(ID, "Ferocity", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 15, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		staminaGain = isUpgraded ? 4 : 3;
		cutoff = isUpgraded ? 14 : 20;
		berserk = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <yellow>" + berserk + "</yellow> " + GlossaryTag.BERSERK.tag(this) + " stacks. " +
				"If you have <yellow>" + cutoff + "</yellow> stacks after, gain <yellow>" + staminaGain + "</yellow> stamina regen"
						+ " and disable the ability.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			pdata.applyStatus(StatusType.BERSERK, data, berserk, -1);
			if (pdata.getStatus(StatusType.BERSERK).getStacks() >= cutoff) {
				Sounds.roar.play(p, p);
				p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		}));
	}
}
