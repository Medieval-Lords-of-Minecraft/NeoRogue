package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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

public class Cryostasis extends Equipment implements Power {
	private static final String ID = "Cryostasis";
	private static final Circle ICE_SHELL = new Circle(1.25);
	private static final ParticleContainer SHELL_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(110, 220, 255), 1.1F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer ICE_SPARK = new ParticleContainer(Particle.SNOWFLAKE)
			.count(12).spread(0.1, 0.1).offsetY(1).speed(0.01);
	private int activationThreshold = 150, frostThreshold, invincibilityDuration = 1;

	public Cryostasis(boolean isUpgraded) {
		super(ID, "Cryostasis", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.none());
		frostThreshold = isUpgraded ? 25 : 30;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta applied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.FROST) || event.getStacks() <= 0) return TriggerResult.keep();
			applied.addCount(event.getStacks());
			if (applied.getCount() < activationThreshold) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player player = data.getPlayer();
		ICE_SHELL.play(SHELL_EDGE, player.getLocation(), LocalAxes.xz(), null);
		ICE_SPARK.play(player, player);
		Sounds.glass.play(player, player);
		ActionMeta applied = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.FROST) || event.getStacks() <= 0) return TriggerResult.keep();
			applied.addCount(event.getStacks());
			while (applied.getCount() >= frostThreshold) {
				applied.addCount(-frostThreshold);
				data.applyStatus(StatusType.INVINCIBLE, data, 1, invincibilityDuration * 20, this);
				Player current = data.getPlayer();
				ICE_SHELL.play(SHELL_EDGE, current.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
				ICE_SPARK.play(current, current);
				Sounds.glass.play(current, current);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLUE_ICE, GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this)
				+ ". Activates after applying " + GlossaryTag.FROST.tag(this, activationThreshold)
				+ ". Once active, every " + GlossaryTag.FROST.tag(this, frostThreshold) + " applied grants "
				+ GlossaryTag.INVINCIBLE.tag(this) + " [" + DescUtil.white(invincibilityDuration + "s") + "].");
	}
}