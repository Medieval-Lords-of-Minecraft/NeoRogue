package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class PocketKnife extends Equipment {
	private static final String ID = "PocketKnife";
	private int damage;

	public PocketKnife(boolean isUpgraded) {
		super(ID, "Pocket Knife", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		damage = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			FightData targetData = FightInstance.getFightData(event.getTarget());
			if (!targetData.hasStatus(StatusType.REND)) return TriggerResult.keep();

			event.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.SLASHING,
					DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHEARS, "Deal an additional " + GlossaryTag.SLASHING.tag(this, damage)
				+ " damage to enemies with " + GlossaryTag.REND.tag(this) + ".");
	}
}