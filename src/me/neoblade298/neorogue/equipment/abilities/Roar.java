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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Roar extends Equipment {
	private static final String ID = "roar";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private int strength, heal;
	private static final int CUTOFF = 20;
	
	public Roar(boolean isUpgraded) {
		super(ID, "Roar", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 15, 0));
		strength = isUpgraded ? 8 : 5;
		heal = isUpgraded ? 10 : 6;
		
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIREWORK_STAR,
				"On cast, give yourself " + GlossaryTag.STRENGTH.tag(this, strength, true) + " and " + GlossaryTag.BERSERK.tag(this, 1, false) + "."
						+ " If above " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + ", also heal for <yellow>" + heal + "</yellow>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.blazeDeath.play(p, p);
			pc.play(p, p);
			data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			data.applyStatus(StatusType.BERSERK, data, 1, -1);
			if (data.getStatus(StatusType.BERSERK).getStacks() >= CUTOFF) {
				FightInstance.giveHeal(p, heal, p);
				Sounds.levelup.play(p, p);
			}
			return TriggerResult.keep();
		}));
	}
}
