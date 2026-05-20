package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class NaturesGift extends Equipment {
	private static final String ID = "NaturesGift";
	private double manaPerTick;

	public NaturesGift(boolean isUpgraded) {
		super(ID, "Nature's Gift", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		manaPerTick = isUpgraded ? 0.8 : 0.5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (FightInstance.isOnGrass(data.getEntity())) {
				data.addMana(manaPerTick);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERN,
				"Gain " + DescUtil.yellow(manaPerTick) + " mana per second while standing on grass.");
	}
}
