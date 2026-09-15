package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class WhateverItTakes extends Equipment {
	private static final String ID = "WhateverItTakes";
	private static final int BURN_THRESHOLD = 30;
	private static final ParticleContainer intellectParticles = new ParticleContainer(Particle.ENCHANT)
			.count(20).spread(0.5, 0.5).offsetY(1).speed(0.01);
	private int intellect;

	public WhateverItTakes(boolean isUpgraded) {
		super(ID, "Whatever It Takes", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		intellect = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta burnProgress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.getStacks() <= 0) return TriggerResult.keep();

			int activations;
			if (ev.isStatus(StatusType.BURN)) {
				burnProgress.addCount(ev.getStacks());
				activations = burnProgress.getCount() / BURN_THRESHOLD;
				burnProgress.setCount(burnProgress.getCount() % BURN_THRESHOLD);
			}
			else if (ev.isStatus(StatusType.CORRUPTION)) {
				activations = ev.getStacks();
			}
			else return TriggerResult.keep();

			if (activations > 0) {
				data.applyStatus(StatusType.INTELLECT, data, activations * intellect, -1, this);
				Player p = data.getPlayer();
				Sounds.enchant.play(p, p);
				intellectParticles.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENCHANTED_BOOK,
				GlossaryTag.PASSIVE.tag(this) + ". Gain " + GlossaryTag.INTELLECT.tag(this, intellect)
						+ " for every " + GlossaryTag.BURN.tag(this, BURN_THRESHOLD) + " or "
						+ GlossaryTag.CORRUPTION.tag(this, 1) + " you apply.");
	}
}