package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TitansVerdict extends Equipment {
	private static final String ID = "TitansVerdict";
	private static final int CHARGE_TICKS = 40;
	private static final TargetProperties TARGETS = TargetProperties.line(10, 2, TargetType.ENEMY);
	private static final ParticleContainer CHARGE = new ParticleContainer(Particle.DUST).count(1).spread(0, 0).speed(0)
			.dustOptions(new DustOptions(Color.fromRGB(225, 197, 120), 1F));
	private static final ParticleContainer SHOCKWAVE = new ParticleContainer(Particle.DUST_PLUME).count(2)
			.spread(0.05, 0.05).speed(0);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.BLOCK).count(8).spread(0.1, 0.1)
			.speed(0.01).blockData(Material.DEEPSLATE.createBlockData());

	public TitansVerdict(boolean isUpgraded) {
		super(ID, "Titan's Verdict", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 70, isUpgraded ? 13 : 18, TARGETS.range));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			data.charge(CHARGE_TICKS, 0);
			Sounds.enchant.play(data.getPlayer(), data.getPlayer());
			data.addTask(new BukkitRunnable() {
				private int ticks;
				@Override
				public void run() {
					Player current = data.getPlayer();
					Location center = current.getLocation().add(0, 1, 0);
					double radius = 1.8 * (CHARGE_TICKS - ticks) / CHARGE_TICKS;
					for (int i = 0; i < 4; i++) {
						double angle = Math.toRadians(i * 90 + ticks * 14);
						CHARGE.play(current, center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
					}
					if (++ticks >= CHARGE_TICKS) cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 1));
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player p = data.getPlayer();
					Vector direction = p.getEyeLocation().getDirection().setY(0).normalize();
					Location start = p.getLocation();
					Location end = start.clone().add(direction.clone().multiply(TARGETS.range));
					double damage = data.getStats().getShieldsApplied();
					Sounds.anvil.play(p, p);
					launchShockwave(data, start, end, direction, damage, slot);
				}
			}.runTaskLater(NeoRogue.inst(), CHARGE_TICKS));
			return TriggerResult.keep();
		}));
	}

	private void launchShockwave(PlayerFightData data, Location start, Location end, Vector direction,
			double damage, int slot) {
		Set<UUID> hit = new HashSet<>();
		data.addTask(new BukkitRunnable() {
			private int distance;
			@Override
			public void run() {
				Player p = data.getPlayer();
				Location front = start.clone().add(direction.clone().multiply(++distance));
				Vector cross = direction.clone().rotateAroundY(Math.toRadians(90));
				ParticleUtil.drawLine(p, SHOCKWAVE, front.clone().add(cross), front.clone().subtract(cross), 0.2);
				for (LivingEntity target : TargetHelper.getEntitiesInLine(p, start, end, TARGETS)) {
					double targetDistance = target.getLocation().toVector().subtract(start.toVector()).dot(direction);
					if (targetDistance > distance + 0.5 || !hit.add(target.getUniqueId())) continue;
					IMPACT.play(p, target.getLocation().add(0, 0.3, 0));
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT,
							DamageStatTracker.of(id + slot, TitansVerdict.this)), target);
				}
				if (distance < TARGETS.range) return;
				Sounds.explode.play(p, front);
				cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), 1, 1));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ANVIL,
				"On cast, " + DescUtil.charge(this, 0, 2) + ", then send out a line shockwave that deals "
						+ GlossaryTag.BLUNT.tag(this) + " damage equal to the total " + GlossaryTag.SHIELDS.tag(this)
						+ " you have applied this fight to every enemy hit.");
	}
}