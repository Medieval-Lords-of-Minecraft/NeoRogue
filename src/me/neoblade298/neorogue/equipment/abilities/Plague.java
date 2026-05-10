package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class Plague extends Equipment {
	private static final String ID = "Plague";
	private int damage, thres, maxThres;
	
	public Plague(boolean isUpgraded) {
		super(ID, "Plague", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 10, 0, 0));
		damage = 10;
		thres = 200;
		maxThres = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			PlagueInstance inst = new PlagueInstance(ID, slot, this);
			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
				ApplyStatusEvent ev = (ApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
				return inst.calculateStacks(ev.getStacks());
			});
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, inst);

			return TriggerResult.remove();
		}));
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
			damageStacks = Math.min(maxThres, stacksApplied / thres);
			if (damageStacks >= maxThres) return TriggerResult.remove();
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS,
				GlossaryTag.POWER.tag(this) + ". Your basic attacks deal an additional " + GlossaryTag.POISON.tag(this, damage, false) +
				" damage for every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.POISON.tag(this) + " you've applied, up to " + DescUtil.yellow(maxThres * thres) + ".");
	}
}
