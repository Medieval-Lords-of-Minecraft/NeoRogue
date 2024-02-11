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
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BerserkersCall extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private int strength, berserkStrength;
	private static final int BERSERK_CUTOFF = 20;
	
	public BerserkersCall(boolean isUpgraded) {
		super("berserkersCall", "Berserker's Call", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 75, 15, 0));
		strength = 20;
		berserkStrength = isUpgraded ? 40 : 30;
		
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, give yourself <white>" + strength + " </white>bonus " + GlossaryTag.PHYSICAL.tag(this) + " damage and <white>1</white>"
						+ " stack of " + GlossaryTag.BERSERK.tag(this) + ". At <white>" + BERSERK_CUTOFF + "</white> stacks, instead give yourself <yellow>" + berserkStrength + 
						"</yellow> bonus " + GlossaryTag.PHYSICAL.tag + " damage.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			pc.spawn(p);
			Status s = data.getStatus("BERSERK");
			if (s != null && s.getStacks() >= BERSERK_CUTOFF) {
				data.addBuff(p.getUniqueId(), id, true, false, BuffType.PHYSICAL, berserkStrength, -1);
			}
			else {
				data.addBuff(p.getUniqueId(), id, true, false, BuffType.PHYSICAL, strength, -1);
			}
			return TriggerResult.keep();
		}));
	}
}
