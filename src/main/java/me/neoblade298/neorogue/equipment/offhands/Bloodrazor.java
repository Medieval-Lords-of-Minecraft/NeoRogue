package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class Bloodrazor extends Equipment {
	private static final String ID = "Bloodrazor";
	private static final int BERSERK_THRESHOLD = 20;
	private static final int STRENGTH_THRESHOLD = 50;
	private static final int STATUS_THRESHOLD = 100;
	private static final double STAMINA_PERCENT = 0.6;
	private static final double ATTACK_SPEED = 0.5;
	private static final TargetProperties TARGETS = TargetProperties.radius(6, false, TargetType.ENEMY);
	private static final ParticleContainer AWAKEN = new ParticleContainer(Particle.DUST).count(18)
			.spread(0.1, 0.1).offsetY(1).speed(0.01)
			.dustOptions(new DustOptions(Color.fromRGB(170, 15, 24), 1.2F));
	private static final ParticleContainer SLASH = new ParticleContainer(Particle.DUST).count(1).spread(0, 0).speed(0)
			.dustOptions(new DustOptions(Color.fromRGB(210, 30, 38), 0.8F));

	public Bloodrazor(boolean isUpgraded) {
		super(ID, "Bloodrazor", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.OFFHAND,
				EquipmentProperties.ofWeapon(isUpgraded ? 120 : 80, ATTACK_SPEED, DamageType.SLASHING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta active = new ActionMeta();
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!active.getBool() && shouldActivate(data)) {
					active.setBool(true);
					Player p = data.getPlayer();
					AWAKEN.play(p, p);
					Sounds.roar.play(p, p);
				}
				if (!active.getBool() || data.getStamina() <= data.getMaxStamina() * STAMINA_PERCENT
						|| !data.canBasicAttack(EquipSlot.OFFHAND)) return;
				Player p = data.getPlayer();
				LivingEntity target = TargetHelper.getNearest(p, TARGETS);
				if (target != null) {
					Location start = p.getLocation().add(0, 1, 0);
					Location end = target.getLocation().add(0, target.getHeight() * 0.6, 0);
					ParticleUtil.drawLine(p, SLASH, start, end, 0.3);
					weaponSwingAndDamage(p, data, target);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0, 1));
	}

	private boolean shouldActivate(PlayerFightData data) {
		return data.getStatus(StatusType.BERSERK).getStacks() > BERSERK_THRESHOLD
				|| data.getStatus(StatusType.STRENGTH).getStacks() > STRENGTH_THRESHOLD
				|| data.getStats().getStatusesApplied().getOrDefault(StatusType.CONCUSSED, 0) >= STATUS_THRESHOLD
				|| data.getStats().getStatusesApplied().getOrDefault(StatusType.SANCTIFIED, 0) >= STATUS_THRESHOLD;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates permanently upon exceeding "
						+ GlossaryTag.BERSERK.tag(this, BERSERK_THRESHOLD) + " or "
						+ GlossaryTag.STRENGTH.tag(this, STRENGTH_THRESHOLD) + ", or after applying "
						+ DescUtil.val(STATUS_THRESHOLD) + " " + GlossaryTag.CONCUSSED.tag(this) + " or "
						+ GlossaryTag.SANCTIFIED.tag(this) + ". While above " + DescUtil.val("60% stamina")
						+ ", automatically basic attack the nearest enemy.");
	}
}