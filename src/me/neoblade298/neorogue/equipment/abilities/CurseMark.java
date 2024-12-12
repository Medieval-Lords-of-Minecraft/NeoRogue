package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class CurseMark extends Equipment {
	private static final String ID = "curseMark";
	private static TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY);
	private static ParticleContainer pc = new ParticleContainer(Particle.SMOKE).count(50).spread(0.5, 2).offsetY(1);
	private int stacks, buff;
	
	public CurseMark(boolean isUpgraded) {
		super(ID, "Curse Mark", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
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
			if (ev.getMeta().containsType(DamageCategory.GENERAL) && ev.getTarget().getUniqueId().equals(inst.mark.getUniqueId())) {
				inst.mark = null;
			}
			return TriggerResult.keep();
		});
	}

	private class InducePanicInstance extends EquipmentInstance	{
		private LivingEntity mark;
		public InducePanicInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			InducePanicInstance inst = this;
			action = (pdata, in) -> {
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						if (mark == null) return;
						Player p = data.getPlayer();
						FightData fd = FightInstance.getFightData(mark);
						FightInstance.applyStatus(mark, StatusType.INSANITY, pdata, stacks, -1);
						pc.play(p, inst.mark);
						Sounds.infect.play(p, mark);
						fd.addDefenseBuff(DamageBuffType.of(DamageCategory.DARK), new Buff(data, 0, -buff * 0.01, StatTracker.defenseDebuffEnemy(eq)), 100);
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
				" apply " + GlossaryTag.INSANITY.tag(this, stacks, false) + " to them and increase " + GlossaryTag.DARK.tag(this) + " damage dealt to them by " +
				DescUtil.yellow(buff + "%") + "[<white>5s</white>].");
	}
}
