package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class ConfidenceKill extends Equipment {
	private static final String ID = "confidenceKill";
	private static TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY);
	private static ParticleContainer pc = new ParticleContainer(Particle.SMOKE).count(50).spread(0.5, 2).offsetY(1);
	private int stacks, buff;
	
	public ConfidenceKill(boolean isUpgraded) {
		super(ID, "Confidence Kill", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, tp.range));
				stacks = 90;
				buff = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		InducePanicInstance inst = new InducePanicInstance(data, this, slot, es);
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (inst.mark == null) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().containsType(DamageBuffType.GENERAL) && ev.getTarget().getUniqueId().equals(inst.mark.getUniqueId())) {
				inst.mark = null;
			}
			return TriggerResult.keep();
		});

		data.addTrigger(ID, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			if (inst.toKill.getUniqueId().equals(ev.getTarget().getUniqueId())) {
				Sounds.extinguish.play(p, p);
				pdata.addBuff(pdata, UUID.randomUUID().toString(), true, true, DamageBuffType.GENERAL, buff, 200);
			}
			return TriggerResult.keep();
		});
	}

	private class InducePanicInstance extends EquipmentInstance	{
		private LivingEntity mark, toKill;
		public InducePanicInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			InducePanicInstance inst = this;
			action = (pdata, in) -> {
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						if (inst.mark == null) return;
						Player p = data.getPlayer();
						FightInstance.applyStatus(inst.mark, StatusType.INSANITY, pdata, stacks, -1);
						pc.play(p, inst.mark);
						Sounds.infect.play(p, inst.mark);
						toKill = mark;
						pdata.addTask(new BukkitRunnable() {
							public void run() {
								toKill = null;
							}
						}.runTaskLater(NeoRogue.inst(), 100L));
					}
				}.runTaskLater(NeoRogue.inst(), 40L));
				return TriggerResult.keep();
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			if (!super.canTrigger(p, data)) return false;
			mark = TargetHelper.getNearestInSight(p, tp);
			return mark != null;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, mark the target you're looking at. If you don't deal " + GlossaryTag.GENERAL.tag(this) + " damage to that enemy for <white>2</white> seconds," +
				" apply " + GlossaryTag.INSANITY.tag(this, stacks, false) + " to them. If you kill this enemy within the next 5 seconds, buff your damage by "
				+ DescUtil.yellow(buff + "%") + " [<white>10s</white>].");
	}
}
