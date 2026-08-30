package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Soulrender extends Equipment {
	private static final String ID = "Soulrender";
	private static final int COOLDOWN = 8;
	private static final ParticleContainer HIT = new ParticleContainer(Particle.SOUL_FIRE_FLAME)
			.count(12).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer BLADE = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(84, 236, 255), 1.15F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer BLADE_FLAME = new ParticleContainer(Particle.SOUL_FIRE_FLAME)
			.count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleAnimation STAB, STAB_FLAME;

	static {
		STAB = new ParticleAnimation(BLADE, (loc, tick) -> stab(loc, tick, false), 6);
		STAB_FLAME = new ParticleAnimation(BLADE_FLAME, (loc, tick) -> stab(loc, tick, true), 6);
	}

	private static LinkedList<Location> stab(Location location, int tick, boolean flames) {
		LinkedList<Location> locations = new LinkedList<>();
		Vector forward = location.getDirection().normalize();
		Vector side = forward.clone().crossProduct(new Vector(0, 1, 0));
		if (side.lengthSquared() < 0.001) side.setX(1);
		side.normalize();
		double extension = tick < 3 ? 0.9 + tick * 0.9 : 3.6 - (tick - 3) * 0.65;
		int points = flames ? 5 : 12;
		for (int point = 1; point <= points; point++) {
			double progress = point / (double) points;
			Vector offset = forward.clone().multiply(extension * progress);
			if (!flames) offset.add(side.clone().multiply(Math.sin(progress * Math.PI) * 0.08));
			locations.add(location.clone().add(offset));
		}
		return locations;
	}
	private int rend;

	public Soulrender(boolean isUpgraded) {
		super(ID, "Soulrender", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(0, 10, COOLDOWN, 0));
		rend = isUpgraded ? 18 : 12;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			LeftClickHitEvent event = (LeftClickHitEvent) in;
			Player player = data.getPlayer();
			LivingEntity target = event.getTarget();
			FightData targetData = FightInstance.getFightData(target);
			int currentRend = targetData.getStatus(StatusType.REND).getStacks();
			player.swingOffHand();
			Location stabLocation = player.getEyeLocation().clone().add(0, -0.25, 0);
			stabLocation.setDirection(target.getEyeLocation().toVector().subtract(stabLocation.toVector()));
			data.runAnimation(id + "-stab", player, STAB, stabLocation);
			data.runAnimation(id + "-stab-flame", player, STAB_FLAME, stabLocation);
			Sounds.attackSweep.play(player, target);
			Sounds.wither.play(player, target);
			HIT.play(player, target);
			if (currentRend > 0) FightInstance.dealDamage(new DamageMeta(data, currentRend, DamageType.PIERCING,
					DamageStatTracker.of(id + slot, this)), target);
			FightInstance.applyStatus(target, StatusType.REND, data, rend, -1, this);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE, "Left click an enemy to deal damage equal to their current "
				+ GlossaryTag.REND.tag(this) + " as " + GlossaryTag.PIERCING.tag(this) + " damage and apply " + GlossaryTag.REND.tag(this, rend) + ".");
	}
}