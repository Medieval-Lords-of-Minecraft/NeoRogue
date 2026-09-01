package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class ForceOfNature extends Equipment {
	private static final String ID = "ForceOfNature";
	private static final int DIRECT_REDUCTION = 2, MANA_THRESHOLD = 100;
	private int healing;

	public ForceOfNature(boolean isUpgraded) {
		super(ID, "Force of Nature", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ARMOR, EquipmentProperties.none());
		healing = isUpgraded ? 3 : 2;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, DIRECT_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot, this)));
		ActionMeta manaSpent = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent event = (CastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			manaSpent.addDouble(event.getInstance().getManaCost());
			while (manaSpent.getDouble() >= MANA_THRESHOLD) {
				manaSpent.addDouble(-MANA_THRESHOLD);
				data.addHealth(healing, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this)
				+ " damage taken by " + DescUtil.val(DIRECT_REDUCTION) + ". For every "
				+ DescUtil.val(MANA_THRESHOLD) + " base mana spent casting abilities, heal " + DescUtil.val(healing) + " health.");
	}
}