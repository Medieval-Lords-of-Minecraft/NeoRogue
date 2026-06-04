package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Dismantle extends Equipment implements Power {
	private static final String ID = "Dismantle";
	private int stacks;
	
	public Dismantle(boolean isUpgraded) {
		super(ID, "Dismantle", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		stacks = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 200;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			am.addDouble(ev.getTotalDamage());
			if (am.getDouble() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	private class DismantleInstance extends PriorityAction {
		private LivingEntity target;
		public DismantleInstance(PlayerFightData data, Equipment equip, int slot, EquipSlot es) {
			super(ID);
			action = (pdata, in) -> {
				DealDamageEvent ev = (DealDamageEvent) in;
				if (ev.getTarget() == target) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks, -1);
				}
				else {
					target = ev.getTarget();
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.DEAL_DAMAGE, new DismantleInstance(data, Dismantle.this, slot, es));
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.IRON_PICKAXE,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.white(200) + " damage. Dealing consecutive damage to an enemy applies " + GlossaryTag.INJURY.tag(this, stacks, true) + ".");
	}
}
