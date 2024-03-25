package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class DarkMask extends Equipment {
	private static final String ID = "darkMask";
	private int enemyAmount, selfAmount;

	public DarkMask(boolean isUpgraded) {
		super(ID, "Dark Mask", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ARMOR);
		enemyAmount = isUpgraded ? 6 : 3;
		selfAmount = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			// why the fUCK can i not get damage recipient from this event; TODO: GET THE RECIPIENT
			Entity target = null;
			FightInstance.applyStatus(target, StatusType.INSANITY, data, enemyAmount, -1);
			FightInstance.applyStatus(p, StatusType.INSANITY, data, selfAmount, -1);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.OBSIDIAN,
				"Dealing damage to enemies applies " + GlossaryTag.INSANITY.tag(this, enemyAmount, true)
						+ " to them and <yellow>" + selfAmount + "</yellow> to you."
		);
	}
}
