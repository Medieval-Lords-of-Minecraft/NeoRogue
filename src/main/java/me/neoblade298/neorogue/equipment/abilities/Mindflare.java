package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Mindflare extends Equipment implements Power {
	private static final String ID = "Mindflare";
	private static final int ACTIVATION_THRESHOLD = 25;
	private static final int BURN_THRESHOLD = 10;
	private static final ParticleContainer INTELLECT_PARTICLES = new ParticleContainer(Particle.ENCHANT)
			.count(20).spread(0.5, 0.5).offsetY(1).speed(0.01);
	private int intellect;

	public Mindflare(boolean isUpgraded) {
		super(ID, "Mindflare", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		intellect = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activationProgress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BURN) || ev.getStacks() <= 0) return TriggerResult.keep();

			activationProgress.addCount(ev.getStacks());
			if (activationProgress.getCount() < ACTIVATION_THRESHOLD) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				ActionMeta burnProgress = new ActionMeta();
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
					if (!ev2.isStatus(StatusType.BURN) || ev2.getStacks() <= 0) return TriggerResult.keep();

					burnProgress.addCount(ev2.getStacks());
					int activations = burnProgress.getCount() / BURN_THRESHOLD;
					burnProgress.setCount(burnProgress.getCount() % BURN_THRESHOLD);
					if (activations <= 0) return TriggerResult.keep();

					data.applyStatus(StatusType.INTELLECT, data, activations * intellect, -1, Mindflare.this);
					Player p = data.getPlayer();
					Sounds.enchant.play(p, p);
					INTELLECT_PARTICLES.play(p, p);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying "
						+ GlossaryTag.BURN.tag(this, ACTIVATION_THRESHOLD) + ". Gain "
						+ GlossaryTag.INTELLECT.tag(this, intellect) + " for every "
						+ GlossaryTag.BURN.tag(this, BURN_THRESHOLD) + " you apply.");
	}
}