package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BitterCold extends Equipment {
	private static final String ID = "bitterCold";
	private int stacks, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN);
	
	public BitterCold(boolean isUpgraded) {
		super(ID, "Bitter Cold", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stacks = isUpgraded ? 5 : 3;
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuff().addIncrease(data, stacks);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE,
				"Passive. All applications of " + GlossaryTag.FROST.tag(this) + " are increased by " + DescUtil.yellow(cooldown) + ". Once per enemy, applying " +
				GlossaryTag.FROST.tag(this, 50, false) + " to them will deal " + GlossaryTag.ICE.tag(this, damage, true) + " damage to them.");
	}
}
