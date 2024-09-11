package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Plague extends Equipment {
	private static final String ID = "plague";
	private int damage, thres, maxThres;
	
	public Plague(boolean isUpgraded) {
		super(ID, "Plague", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = 10;
		thres = 200;
		maxThres = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		PlagueInstance inst = new PlagueInstance(ID);
		data.addTrigger(ID, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			return inst.calculateStacks(ev.getStacks());
		});
		data.addTrigger(ID, Trigger.BASIC_ATTACK, inst);
	}

	private class PlagueInstance extends PriorityAction	{
		private int damageStacks, stacksApplied;
		public PlagueInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (damageStacks > 0) {
					BasicAttackEvent ev = (BasicAttackEvent) in;
					ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * damageStacks, DamageType.POISON));
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
				"Passive. Your basic attacks deal an additional " + GlossaryTag.POISON.tag(this, damage, true) +
				" damage for every " + thres + " stacks of " + GlossaryTag.POISON.tag(this) + " you've applied, up to " + (maxThres * thres) + ".");
	}
}
