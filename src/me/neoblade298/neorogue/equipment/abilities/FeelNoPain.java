package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class FeelNoPain extends Equipment {
	private static final String ID = "feelNoPain";
	private double reduc;
	private int reducString;
	private static final int THRES = 10, COUNT = 4, CUTOFF = THRES * COUNT;
	
	public FeelNoPain(boolean isUpgraded) {
		super(ID, "Feel No Pain", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		reduc = isUpgraded ? 0.08 : 0.05;
		reducString = (int) (reduc * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			int stacks = data.getStatus(StatusType.BERSERK).getStacks();
			int ct = Math.min(COUNT, stacks / THRES);
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, ct * reduc, null));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_HELMET,
				"Passive. Gain <yellow>" + reducString + "%</yellow> damage reduction for every <white>" + THRES + "</white> stacks of " + GlossaryTag.BERSERK.tag(this) + ", up to " +
				DescUtil.white(CUTOFF) + " stacks.");
	}
}
