package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BerserkersCall extends Equipment {
	private static final String ID = "BerserkersCall";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_BLAZE_DEATH);
	private int strength, berserkStrength;
	private static final int BERSERK_CUTOFF = 16;
	
	public BerserkersCall(boolean isUpgraded) {
		super(ID, "Berserker's Call", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 20, 15, 0));
		strength = 4;
		berserkStrength = isUpgraded ? 16 : 12;
		
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, give yourself " + DescUtil.white(strength) + " " + GlossaryTag.STRENGTH.tag(this) + " and "
						+ GlossaryTag.BERSERK.tag(this, 1, false) + ". At " + DescUtil.white(BERSERK_CUTOFF) + " stacks, instead give yourself " + DescUtil.yellow(berserkStrength) + 
						" " + GlossaryTag.STRENGTH.tag + ".");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			sc.play(p, p);
			pc.play(p, p);
			data.applyStatus(StatusType.BERSERK, data, 1, -1);
			Status s = data.getStatus(StatusType.BERSERK);
			if (s != null && s.getStacks() >= BERSERK_CUTOFF) {
				data.applyStatus(StatusType.STRENGTH, data, berserkStrength, -1);
			}
			else {
				data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			}
			return TriggerResult.keep();
		}));
	}
}
