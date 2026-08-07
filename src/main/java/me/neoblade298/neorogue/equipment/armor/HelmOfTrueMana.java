package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class HelmOfTrueMana extends Equipment {
	private static final String ID = "HelmOfTrueMana";
	private static final int COOLDOWN_THRESHOLD = 7, COOLDOWN_REDUCTION = 30;
	private int shields;

	public HelmOfTrueMana(boolean isUpgraded) {
		super(ID, "Helm of True Mana", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = id + slot;
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY
					|| ev.getInstance().getEquipment().getProperties().get(PropertyType.COOLDOWN) <= COOLDOWN_THRESHOLD) {
				return TriggerResult.keep();
			}

			ev.addBuff(PropertyType.COOLDOWN, buffId,
					Buff.multiplier(data, COOLDOWN_REDUCTION * 0.01,
							BuffStatTracker.of(buffId, this, PropertyType.COOLDOWN.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();

			Player player = data.getPlayer();
			data.addPermanentShield(player.getUniqueId(), shields, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_HELMET, "Reduce ability cooldowns over "
				+ DescUtil.white(COOLDOWN_THRESHOLD + "s") + " by " + DescUtil.white(COOLDOWN_REDUCTION + "%")
				+ ". Casting an ability grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}
}