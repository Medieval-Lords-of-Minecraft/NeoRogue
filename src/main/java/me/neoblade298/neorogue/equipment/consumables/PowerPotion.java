package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ActivatePowerEvent;

public class PowerPotion extends Consumable {
	private static final String ID = "PowerPotion";
	private int uses;

	public PowerPotion(boolean isUpgraded) {
		super(ID, "Power Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		uses = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		int[] remaining = { uses };
		data.addTrigger(id, Trigger.ACTIVATE_POWER, (pdata, in) -> {
			ActivatePowerEvent ev = (ActivatePowerEvent) in;
			Equipment eq = ev.getEquipment();
			if (eq instanceof Power power) {
				Player p2 = data.getPlayer();
				Sounds.success.play(p2, p2);
				power.onPowerActivated(data, ev.getSlot(), ev.getEquipSlot());
			}
			return --remaining[0] <= 0 ? TriggerResult.remove() : TriggerResult.keep();
		});
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, isUpgraded
				? "Your next [" + DescUtil.yellow(uses) + "] power activations are each triggered twice. Consumed on first use."
				: "Your next power activation is triggered twice. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 69, 0));
		item.setItemMeta(meta);
	}
}
