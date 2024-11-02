package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class HailCloak extends Equipment {
	private static final String ID = "hailCloak";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private int damage, stacks;
	
	public HailCloak(boolean isUpgraded) {
		super(ID, "Hail Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 5, 12, 5));
		damage = isUpgraded ? 30 : 20;
		stacks = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		HailCloakInstance inst = new HailCloakInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!inst.active || !ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			inst.runEffect(ev.getTarget().getLocation());
			return TriggerResult.keep();
		});
	}

	private class HailCloakInstance extends EquipmentInstance {
		boolean active = false;
		public HailCloakInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);

			action = (pdata, in) -> {
				data.channel(20);
				Sounds.equip.play(p, p);
				data.addTask(new BukkitRunnable() {
					public void run() {
						active = true;
						data.addTask(createRunnable());
					}
				}.runTaskLater(NeoRogue.inst(), 20));
				return TriggerResult.keep();
			};
		}

		public BukkitTask createRunnable() {
			return new BukkitRunnable() {
				int tick = 0;
				public void run() {
					runEffect(p.getLocation());
					if (++tick >= 5) {
						active = false;
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20, 20);
		}

		public void runEffect(Location loc) {
			circ.play(pc, loc, LocalAxes.xz(), null);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.ICE), ent);
				FightInstance.applyStatus(ent, StatusType.FROST, data, stacks, -1);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE,
				"On cast, after channeling for <white>1s</white>, for the next <white>5s</white>, you deal " + GlossaryTag.ICE.tag(this, damage, true) + " and apply " +
				GlossaryTag.FROST.tag(this, stacks, true) + " to all enemies near you every second. Your projectiles also do this on hit while active.");
	}
}
