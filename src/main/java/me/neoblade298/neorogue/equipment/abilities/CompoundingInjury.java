package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class CompoundingInjury extends Equipment implements Power {
	private static final String ID = "CompoundingInjury";
	private int multStr, thres;
	private double mult;
	
	public CompoundingInjury(boolean isUpgraded) {
		super(ID, "Compounding Injury", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				thres = isUpgraded ? 20 : 30;
				mult = isUpgraded ? 1.25 : 0.8;
				multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 30;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			am.addCount(ev.getStacks());
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
			DealDamageEvent ev2 = (DealDamageEvent) in2;
			FightData fd = FightInstance.getFightData(ev2.getTarget());
			if (ev2.getMeta().isSecondary())
				return TriggerResult.keep();
			if (fd.getStatus(StatusType.CONCUSSED).getStacks() >= thres && !ev2.getMeta().getPrimarySlice().getType().getCategories().contains(DamageCategory.STATUS)) {
				DamageMeta dm = ev2.getMeta().clone();
				dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, mult, 
						BuffStatTracker.ignored(this)));
				dm.isSecondary(true);

				for (DamageSlice slice : dm.getSlices()) {
					slice.setTracker(DamageStatTracker.of(id + slot, this));
				}
				data.addTask(new BukkitRunnable() {
					public void run() {
						FightInstance.dealDamage(dm, ev2.getTarget());
					}
				}.runTaskLater(NeoRogue.inst(), 20));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
	item = createItem(Material.REDSTONE_ORE,
			GlossaryTag.POWER.tag(this) + ". Activates after applying " + DescUtil.white(ACTIVATION_THRES) + " " + GlossaryTag.CONCUSSED.tag(this) + " stacks. Dealing damage to an enemy with at least " + GlossaryTag.CONCUSSED.tag(this, thres, true) + " will cause the damage to happen again, but "
			+ "multiplied by " + DescUtil.yellow(multStr + "%") + ".");
	}
}
