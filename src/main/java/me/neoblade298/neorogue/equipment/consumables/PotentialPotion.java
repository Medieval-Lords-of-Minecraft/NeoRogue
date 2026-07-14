package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ActivatePowerEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PotentialPotion extends Consumable {
	private static final String ID = "PotentialPotion";
	private int uses;

	public PotentialPotion(boolean isUpgraded) {
		super(ID, "Potential Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
		uses = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		int[] remaining = { uses };
		boolean[] duplicating = { false };
		data.addTrigger(id, Trigger.ACTIVATE_POWER, (pdata, in) -> {
			// Ignore activations we ourselves cause, so a power is only ever duplicated, never triplicated
			if (duplicating[0]) return TriggerResult.keep();
			ActivatePowerEvent ev = (ActivatePowerEvent) in;
			Equipment eq = ev.getEquipment();
			if (!(eq instanceof Power power)) return TriggerResult.keep();

			Player p2 = data.getPlayer();
			Sounds.success.play(p2, p2);
			Util.msgRaw(p2, Component.empty().append(eq.getHoverable())
					.append(Component.text(" was duplicated by ", NamedTextColor.GRAY).append(hoverable)));

			duplicating[0] = true;
			power.onPowerActivated(data, ev.getSlot(), ev.getEquipSlot());
			duplicating[0] = false;

			return --remaining[0] <= 0 ? TriggerResult.remove() : TriggerResult.keep();
		});
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, isUpgraded
				? "Your next [" + DescUtil.yellow(uses) + "] " + GlossaryTag.POWER.tag(this) + " activations are each triggered twice. Consumed on first use."
				: "Your next power activation is triggered twice. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 69, 0));
		item.setItemMeta(meta);
	}
}
