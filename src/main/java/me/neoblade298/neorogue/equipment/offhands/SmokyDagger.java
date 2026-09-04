package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SmokyDagger extends Equipment {
	private static final String ID = "SmokyDagger";
	private static final TargetProperties TARGETS = TargetProperties.radius(20, false, TargetType.ENEMY);
	private static final ParticleContainer STRIKE = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(58, 52, 68), 0.8F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.SMOKE)
			.count(8).spread(0.1, 0.1).speed(0.01);
	private int damage;

	public SmokyDagger(boolean isUpgraded) {
		super(ID, "Smoky Dagger", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		damage = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.EVADE, data, 1, -1, this);
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			attackNearest(data, slot);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			attackNearest(data, slot);
			return TriggerResult.keep();
		});
	}

	private void attackNearest(PlayerFightData data, int slot) {
		Player player = data.getPlayer();
		LivingEntity target = TargetHelper.getNearest(player, TARGETS);
		if (target == null) return;
		ParticleUtil.drawLine(player, STRIKE, player.getLocation().add(0, 1, 0),
				target.getLocation().add(0, target.getHeight() * 0.5, 0), 0.35);
		IMPACT.play(player, target.getLocation().add(0, target.getHeight() * 0.5, 0));
		Sounds.attackSweep.play(player, target);
		FightInstance.dealDamage(data, DamageType.DARK, damage, target, DamageStatTracker.of(id + slot, this));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, GlossaryTag.PASSIVE.tag(this) + ". Whenever you "
				+ GlossaryTag.DASH.tag(this) + " or " + GlossaryTag.EVADE.tag(this) + ", deal "
				+ GlossaryTag.DARK.tag(this, damage) + " damage to the nearest enemy. Start fights with "
				+ GlossaryTag.EVADE.tag(this, 1) + ".");
	}
}