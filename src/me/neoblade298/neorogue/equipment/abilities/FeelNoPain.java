package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class FeelNoPain extends Equipment {
	private static final String ID = "FeelNoPain";
	private double reduc;
	private int reducString;
	private static final int THRES = 10, COUNT = 4, CUTOFF = THRES * COUNT;
	
	public FeelNoPain(boolean isUpgraded) {
		super(ID, "Feel No Pain", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 20, 0, 0));
		reduc = isUpgraded ? 0.08 : 0.05;
		reducString = (int) (reduc * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata2, in2) -> {
				ReceiveDamageEvent ev = (ReceiveDamageEvent) in2;
				int stacks = data.getStatus(StatusType.BERSERK).getStacks();
				int ct = Math.min(COUNT, stacks / THRES);
				ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.multiplier(data, ct * reduc, null));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_HELMET,
				GlossaryTag.POWER.tag(this) + ". Gain " + DescUtil.yellow(reducString + "%") + " damage reduction for every " + DescUtil.white(THRES) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", up to " +
				DescUtil.white(CUTOFF) + " stacks.");
	}
}
