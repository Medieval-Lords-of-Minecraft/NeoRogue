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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Ferocity extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private int staminaGain, cutoff, berserk;
	
	public Ferocity(boolean isUpgraded) {
		super("ferocity", "Ferocity", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 50, 15, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		staminaGain = isUpgraded ? 4 : 3;
		cutoff = isUpgraded ? 14 : 20;
		berserk = 3;
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
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			pc.spawn(p);
			pdata.applyStatus(GenericStatusType.BASIC, "BERSERK", p.getUniqueId(), berserk, -1);
			if (pdata.getStatus("BERSERK").getStacks() >= cutoff) {
				Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1F, 1F, false);
				p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		}));
	}
}
