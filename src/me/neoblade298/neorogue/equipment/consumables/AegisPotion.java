package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AegisPotion extends Consumable {
	private static final String ID = "AegisPotion";
	private static final int TICK_INTERVAL = 60; // 3 seconds (20 ticks/sec * 3)
	private double upfront, periodic;

	public AegisPotion(boolean isUpgraded) {
		super(ID, "Aegis Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		upfront = isUpgraded ? 30 : 20;
		periodic = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void runConsumableEffects(Player p, PlayerFightData data, int slot) {
		// Upfront shields
		data.addPermanentShield(p.getUniqueId(), upfront);

		// Periodic shields every 3 seconds
		int[] ticks = { 0 };
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			ticks[0]++;
			if (ticks[0] >= TICK_INTERVAL) {
				ticks[0] = 0;
				Player p2 = data.getPlayer();
				data.addPermanentShield(p2.getUniqueId(), periodic);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Applies " + GlossaryTag.SHIELDS.tag(this, upfront, true) +
				" and then " + GlossaryTag.SHIELDS.tag(this, periodic, true) +
				" every [<white>3s</white>] for the rest of combat. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(100, 149, 237));
		item.setItemMeta(meta);
	}
}
