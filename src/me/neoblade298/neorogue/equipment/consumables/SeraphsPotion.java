package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SeraphsPotion extends Consumable {
	private static final String ID = "SeraphsPotion";
	private static SoundContainer sc = new SoundContainer(Sound.AMBIENT_UNDERWATER_ENTER);
	private int duration;

	public SeraphsPotion(boolean isUpgraded) {
		super(ID, "Seraph's Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
		duration = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.applyStatus(StatusType.INVINCIBLE, data, 1, duration * 20);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (data.hasStatus(StatusType.INVINCIBLE)) return;
				sc.play(p, p);
				Util.msg(p, Component.empty().append(hoverable).append(Component.text(" has expired", NamedTextColor.GRAY)));
			}
		}.runTaskLater(NeoRogue.inst(), duration * 20 + 1L);
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Grants invulnerability for [<yellow>" + duration + "s</yellow>]. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 255, 255));
		item.setItemMeta(meta);
	}
}
