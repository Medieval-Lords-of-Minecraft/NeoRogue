package me.neoblade298.neorogue.equipment.offhands;

import java.util.ArrayDeque;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class SkycallerGlove extends Equipment {
	private static final String ID = "SkycallerGlove";
	private static final int HISTORY_MILLIS = 3000, SKY_HEIGHT = 10, COOLDOWN = 15;
	private static final TargetProperties TARGETS = TargetProperties.cone(30, 10, false, TargetType.ENEMY);
	private static final ParticleContainer SKY_BREAK = new ParticleContainer(Particle.CLOUD)
			.count(16).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer RAIN_PATH = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer TARGET_FLASH = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 214, 92), 1.2F))
			.count(12).spread(0.1, 0.1).speed(0.01);

	public SkycallerGlove(boolean isUpgraded) {
		super(ID, "Skycaller Glove", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(20, 25, COOLDOWN, TARGETS.range));
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ArrayDeque<RecentShot> history = new ArrayDeque<>();
		ActionMeta replaying = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			if (replaying.getBool() || (!event.isBasicAttack() && !event.isAftershot())) return TriggerResult.keep();
			long now = System.currentTimeMillis();
			history.addLast(new RecentShot(event.getGroup(), now, event.isAftershot()));
			trimHistory(history, now);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.LEFT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			LivingEntity target = TargetHelper.getEntitiesInCone(player, TARGETS).peekFirst();
			if (target == null) return TriggerResult.keep();
			long now = System.currentTimeMillis();
			trimHistory(history, now);
			if (history.isEmpty()) return TriggerResult.keep();
			player.swingOffHand();
			Location origin = target.getLocation().clone().add(0, SKY_HEIGHT, 0);
			Location destination = target.getEyeLocation();
			SKY_BREAK.play(player, origin);
			TARGET_FLASH.play(player, destination);
			ParticleUtil.drawLine(player, RAIN_PATH, origin, destination, 0.35);
			Sounds.wind.play(player, origin);
			Sounds.thunder.play(player, destination);
			replaying.setBool(true);
			try {
				for (RecentShot shot : history) {
					Vector direction = target.getEyeLocation().toVector().subtract(origin.toVector()).normalize();
					data.launchAftershot(shot.group(), origin, direction);
				}
			} finally {
				replaying.setBool(false);
			}
			return TriggerResult.keep();
		}, (player, pdata, in) -> {
			trimHistory(history, System.currentTimeMillis());
			return history.stream().anyMatch(RecentShot::aftershot);
		}));
	}

	private void trimHistory(ArrayDeque<RecentShot> history, long now) {
		while (!history.isEmpty() && now - history.getFirst().launchedAt() > HISTORY_MILLIS) history.removeFirst();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_HORSE_ARMOR, "Left click towards an enemy to rain all basic attack projectiles and "
				+ GlossaryTag.AFTERSHOT.tagPlural(this) + " launched in the last "
				+ DescUtil.val("3s") + " from the sky onto the aimed enemy within "
				+ DescUtil.val((int) TARGETS.range) + " blocks. Requires at least one "
				+ GlossaryTag.AFTERSHOT.tag(this) + " to have been launched.");
	}

	private record RecentShot(ProjectileGroup group, long launchedAt, boolean aftershot) {}
}