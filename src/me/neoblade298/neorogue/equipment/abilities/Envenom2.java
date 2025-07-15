package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Envenom2 extends Equipment {
	private static final String ID = "envenom2";
	private int poison;
	
	public Envenom2(boolean isUpgraded) {
		super(ID, "Envenom II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		poison = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!DamageCategory.PHYSICAL.hasType(ev.getMeta().getPrimarySlice().getType())) return TriggerResult.keep();
			FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, poison, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GREEN_DYE,
				"Passive. All primarily " + GlossaryTag.PHYSICAL.tag(this) + " damage you deal applies " + GlossaryTag.POISON.tag(this, poison, true) + ".");
	}
}
