package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Plague extends Equipment implements Power {
	private static final String ID = "Plague";
	private int damage, thres, maxMult;
	
	public Plague(boolean isUpgraded) {
		super(ID, "Plague", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = 2;
		thres = 25;
		maxMult = isUpgraded ? 9 : 6;
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

	private class PlagueInstance extends PriorityAction	{
		private int damageStacks, stacksApplied;
		public PlagueInstance(String id, int slot, Equipment eq) {
			super(id);
			action = (pdata, in) -> {
				if (damageStacks > 0) {
					PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
					ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * damageStacks, DamageType.POISON, DamageStatTracker.of(ID + slot, eq)));
				}
				return TriggerResult.keep();
			};
		}

		public TriggerResult calculateStacks(int added) {
			stacksApplied += added;
			damageStacks = Math.min(maxMult * thres, stacksApplied / thres);
			if (damageStacks >= maxMult * thres) return TriggerResult.remove();
			return TriggerResult.keep();
		}
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		PlagueInstance inst = new PlagueInstance(ID, slot, this);
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
					if (!ev2.isStatus(StatusType.POISON)) return TriggerResult.keep();
					return inst.calculateStacks(ev2.getStacks());
				});
			}
		}.runTask(NeoRogue.inst()));
		data.addTrigger(id + "-attack", Trigger.PRE_BASIC_ATTACK, inst);
	}


	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.POISON.tag(this) + " " + DescUtil.white(3) + " times. Your basic attacks deal an additional " + GlossaryTag.POISON.tag(this, damage, false) +
				" damage for every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.POISON.tag(this) + " you've applied this fight, up to " + DescUtil.yellow(maxMult * thres) + " stacks.");
	}
}
