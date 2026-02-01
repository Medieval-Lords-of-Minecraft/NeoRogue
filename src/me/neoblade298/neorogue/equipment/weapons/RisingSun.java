package me.neoblade298.neorogue.equipment.weapons;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.papermc.paper.entity.TeleportFlag;
import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.GuardianSpirit;
import me.neoblade298.neorogue.equipment.abilities.HerculeanStrength;
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

public class RisingSun extends Equipment {
	private static final String ID = "RisingSun";
	private static final ParticleContainer lancePart = new ParticleContainer(Particle.ELECTRIC_SPARK).count(5).spread(0.1, 0.1);
	private static final TargetProperties tp = TargetProperties.cone(60, 6, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, 60);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5F);
	private int damage;

	public RisingSun(boolean isUpgraded) {
		super(
				ID, "Rising Sun", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 45, 10, tp.range)
		);
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(GuardianSpirit.get(), HolySpear.get());
		addReforge(HerculeanStrength.get(), Condemn.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		DamageStatTracker tracker = DamageStatTracker.of(id + slot, eq);
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			HashSet<UUID> entitiesHit = new HashSet<UUID>();
			data.charge(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					for (int i = 0; i < 6; i++) {
						new BukkitRunnable() {
							public void run() {
								sc.play(p, p);
								cone.play(lancePart, p.getLocation(), LocalAxes.usingGroundedEyeLocation(p), null);
								LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInCone(p, tp);
								for (LivingEntity target : targets) {
									if (entitiesHit.contains(target.getUniqueId())) continue;
									entitiesHit.add(target.getUniqueId());
									DamageMeta dm = new DamageMeta(data, damage, DamageType.PIERCING, tracker);
									FightInstance.dealDamage(dm, target);
									FightInstance.knockback(target, new Vector(0, 3, 0));
								}
								// Rotate player
								Location loc = p.getLocation().clone();
								loc.setYaw(loc.getYaw() + 60);
								p.teleport(loc, TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z, TeleportFlag.Relative.PITCH);
							}
						}.runTaskLater(NeoRogue.inst(), i * 3);
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
				"On cast, " + DescUtil.charge(this, 1, 1) + ", then spin clockwise, dealing " + GlossaryTag.SLASHING.tag(this, damage, true) + " damage to and knocking up all enemies in a circle.");
	}
}
