package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Riftward extends Equipment implements Power {
	private static final String ID = "Riftward";
	private static final Circle WARD_RING = new Circle(1.05);
	private static final ParticleContainer WARD_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(135, 105, 220), 1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer WARD_SPARK = new ParticleContainer(Particle.FIREWORK)
			.count(4).spread(0.1, 0.1).speed(0.01).offsetY(0.9);
	private int activationRifts, shields, durationTicks;

	public Riftward(boolean isUpgraded) {
		super(ID, "Riftward", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		activationRifts = 1;
		shields = isUpgraded ? 4 : 3;
		durationTicks = 120;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id + "-create", Trigger.CREATE_RIFT, (pdata, in) -> {
			grant(data);
			return TriggerResult.keep();
		});
		data.addTrigger(id + "-remove", Trigger.REMOVE_RIFT, (pdata, in) -> {
			grant(data);
			return TriggerResult.keep();
		});
	}

	private void grant(PlayerFightData data) {
		Player p = data.getPlayer();
		data.addSimpleShield(p.getUniqueId(), shields, durationTicks, this);
		WARD_RING.play(WARD_EDGE, p.getLocation().clone().add(0, 1, 0), LocalAxes.xz(), null);
		WARD_SPARK.play(p, p.getLocation());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after creating "
				+ DescUtil.val(activationRifts) + " " + GlossaryTag.RIFT.tag(this) + ". Whenever a rift is created or removed, gain "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " [" + DescUtil.val("6s") + "].");
	}
}
