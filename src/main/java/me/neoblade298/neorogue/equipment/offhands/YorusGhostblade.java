package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

public class YorusGhostblade extends Equipment {
	private static final String ID = "YorusGhostblade";
	private static final ParticleContainer SLASH_PARTICLE = new ParticleContainer(Particle.CHERRY_LEAVES)
			.count(1).spread(0.05, 0.05).speed(0);
	private static final ParticleAnimation SLASH;
	private final int damagePerStealth;

	static {
		SLASH = new ParticleAnimation(SLASH_PARTICLE, (loc, tick) -> {
			LinkedList<Location> locations = new LinkedList<Location>();
			Vector side = loc.getDirection().setY(0);
			if (side.lengthSquared() < 0.001) side.setZ(1);
			side.normalize().rotateAroundY(Math.PI / 2);
			for (int trail = 0; trail < 4; trail++) {
				double angle = Math.toRadians(-65 + tick * 28 - trail * 5);
				Vector offset = side.clone().multiply(Math.cos(angle) * 1.15);
				offset.setY(Math.sin(angle) * 1.15);
				locations.add(loc.clone().add(offset));
			}
			return locations;
		}, 6);
	}

	public YorusGhostblade(boolean isUpgraded) {
		super(ID, "Yoru's Ghostblade", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.OFFHAND,
				EquipmentProperties.ofWeapon(40, 0.5, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damagePerStealth = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta stealthApplied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.isStatus(StatusType.STEALTH) && ev.getTarget() == data && ev.getStacks() > 0) {
				stealthApplied.addCount(ev.getStacks());
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, in) -> {
			RightClickHitEvent ev = (RightClickHitEvent) in;
			Player player = data.getPlayer();
			weaponSwingAndDamage(player, data, ev.getTarget(), properties.get(PropertyType.DAMAGE)
					+ stealthApplied.getCount() * damagePerStealth);
			Location slashLocation = ev.getTarget().getLocation().add(0, 1, 0);
			slashLocation.setDirection(ev.getTarget().getLocation().toVector()
					.subtract(player.getLocation().toVector()));
			data.runAnimation(id + "-slash", player, SLASH, slashLocation);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD, "Right click to attack. Increase this weapon's damage by "
				+ DescUtil.val(damagePerStealth) + " for each stack of " + GlossaryTag.STEALTH.tag(this)
				+ " you apply to yourself.");
	}
}