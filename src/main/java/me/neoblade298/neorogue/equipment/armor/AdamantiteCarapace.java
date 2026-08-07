package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class AdamantiteCarapace extends Equipment {
	private static final String ID = "AdamantiteCarapace";
	private static final int SPRINT_COST_INCREASE = 2;
	private int directReduction;

	public AdamantiteCarapace(boolean isUpgraded) {
		super(ID, "Adamantite Carapace", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		directReduction = isUpgraded ? 6 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, directReduction,
				StatTracker.defenseBuffAlly(id + slot, this)));
		data.addSprintCost(SPRINT_COST_INCREASE);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this)
				+ " damage taken by " + DescUtil.yellow(directReduction)
				+ ". Increase sprinting stamina cost by " + DescUtil.white(SPRINT_COST_INCREASE) + ".");
	}
}