package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class RingOfFortitude extends Equipment {
	private static final String ID = "ringOfFortitude";
	private double mult;
	public RingOfFortitude(boolean isUpgraded) {
		super(ID, "Ring Of Fortitude", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		mult = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getShields().isEmpty()) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, data.getShields().getAmount(), DamageType.BLUNT));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Your basic attacks additionally deal your current " + GlossaryTag.SHIELDS.tag(this)
				+ " multiplied by <yellow>" + mult + "</yellow> as " + GlossaryTag.BLUNT.tag(this) + " damage.");
	}
}
