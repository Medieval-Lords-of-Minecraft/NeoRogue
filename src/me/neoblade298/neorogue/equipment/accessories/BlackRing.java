package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BlackRing extends Equipment {
	private static final String ID = "blackRing";
	private static final int insanityNeeded = 10;
	private int heal;

	public BlackRing(boolean isUpgraded) {
		super(ID, "Black Ring", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		heal = isUpgraded ? 10 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			// todo because still no way to get recipient
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.CHARCOAL,
				"Whenever you deal magic damage, heal <yellow>" + heal + "</yellow> for every <white>" + insanityNeeded
						+ "</white> " + GlossaryTag.INSANITY.tag(this) + " on the target."
		);
	}
}
