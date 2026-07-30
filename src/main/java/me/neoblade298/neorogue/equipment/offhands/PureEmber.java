package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class PureEmber extends Equipment {
	private static final String ID = "PureEmber";
	private int attacks, damage;

	public PureEmber(boolean isUpgraded) {
		super(ID, "Pure Ember", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(10, 0, 16, 0));
		attacks = isUpgraded ? 7 : 5;
		damage = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			am.setCount(attacks);
			Sounds.enchant.play(player, player);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (am.getCount() <= 0) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, this)));
			am.addCount(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"On right click, your next " + DescUtil.val(attacks) + " basic attacks deal an additional "
				+ GlossaryTag.LIGHT.tag(this, damage) + " damage.");
	}
}