package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class BlightTendril extends Equipment implements Power {
	private static final String ID = "BlightTendril";
	private static final TargetProperties tp = TargetProperties.radius(15, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 50, 150), 1F))
			.count(5).spread(0.1, 0.1);
	
	private int poison, poisonDuration;
	private double poisonMult;

	public BlightTendril(boolean isUpgraded) {
		super(ID, "Blight Tendril", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		poison = isUpgraded ? 30 : 20;
		poisonMult = 0.5;
		poisonDuration = 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 3) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	private class BlightTendrilProjectile extends Projectile {
		private PlayerFightData data;
		private String statusName;

		public BlightTendrilProjectile(PlayerFightData data, Equipment eq, int slot, String statusName) {
			super(15, 3);
			this.homing(0.02); // Light homing
			this.blocksPerTick(0.2);
			this.data = data;
			this.statusName = statusName;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Apply poison
			FightInstance.applyStatus(target, StatusType.POISON, data, poison, poisonDuration);
			
			// Mark the enemy with custom status
			FightData fd = FightInstance.getFightData(target);
			if (!fd.hasStatus(statusName)) {
				Sounds.infect.play(data.getPlayer(), target);
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, 1, 160); // 8 seconds duration
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(data.getPlayer(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String statusName = data.getPlayer().getName() + "-blighttendril";
		data.addTask(new BukkitRunnable() {
			public void run() {
				Player p = data.getPlayer();
				// Find nearest enemy
				LivingEntity nearest = TargetHelper.getNearest(p, tp);
				if (nearest == null) return;

				// Fire projectile
				ProjectileGroup proj = new ProjectileGroup(
						new BlightTendrilProjectile(data, BlightTendril.this, slot, statusName));
				proj.start(data, p.getLocation().add(0, 1, 0),
						nearest.getEyeLocation().toVector().subtract(p.getLocation().toVector()).normalize());

				Sounds.shoot.play(p, p);
			}
		}.runTaskTimer(NeoRogue.inst(), 0, 80));

		// Consume mark on basic attack and triple poison
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			PreBasicAttackEvent ev2 = (PreBasicAttackEvent) in2;
			FightData fd = FightInstance.getFightData(ev2.getTarget());

			if (fd.hasStatus(statusName)) {
				// Target is marked - consume mark and triple poison
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, -1, -1);

				// Apply 2x the existing poison stacks (resulting in 3x total)
				if (fd.hasStatus(StatusType.POISON)) {
					int existingPoison = fd.getStatus(StatusType.POISON).getStacks();
					FightInstance.applyStatus(ev2.getTarget(), StatusType.POISON, data, (int) (existingPoison * poisonMult), 100);
					pc.play(p2, ev2.getTarget());
				}
			}

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
	item = createItem(Material.VINE,
			GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.POISON.tag(this) + " " + DescUtil.white(3) + " times. Every " + DescUtil.white(4) + " seconds, summon a lightly homing projectile towards the nearest enemy within " + DescUtil.white(15) + " blocks that " +
			"applies " + GlossaryTag.POISON.tag(this, poison, true) + " [" + DescUtil.white(poisonDuration / 20 + "s") + "] and marks them [<white>8s</white>]. " +
				"Basic attacks consume the mark and apply an additional " + DescUtil.white((int) (poisonMult * 100) + "%") + " their current " + GlossaryTag.POISON.tag(this) + ".");
	}
}
