package me.neoblade298.neorogue.equipment.consumables;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CatalystPotion extends Consumable {
	private static final String ID = "CatalystPotion";
	private int count;

	public CatalystPotion(boolean isUpgraded) {
		super(ID, "Catalyst Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		count = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		PlayerSessionData sdata = data.getSessionData();
		Session s = sdata.getSession();
		int value = s.getBaseDropValue();

		// Roll random powers at the session's current drop level
		ArrayList<Equipment> rolled = Equipment.getPower(value, count, sdata.getPlayerClass(), EquipmentClass.CLASSLESS);
		if (rolled.isEmpty()) return TriggerResult.remove();

		// activatePower gives each its own activation feedback (message + sound) and fires the
		// PRE_ACTIVATE_POWER/ACTIVATE_POWER triggers, so activations behave like normal power procs
		for (Equipment eq : rolled) {
			((Power) eq).activatePower(data, slot, EquipSlot.HOTBAR);
		}
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On use, activate " + DescUtil.yellow(count) + " random " + GlossaryTag.POWER.tag(this)
				+ (count == 1 ? "" : "s") + " at your current drop level.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(170, 60, 220));
		item.setItemMeta(meta);
	}
}
