package me.neoblade298.neorogue.ascension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class UpgradeHolder {
	private String id, display, description;
	private String[] otherUpgrades;
	private UpgradeRequirement req;
	private int slot;
	private Material mat;
	private boolean has, canGet;
	private Upgrade upgrade;
	
	public UpgradeHolder(Upgrade upgrade, String display, String description, int slot, Material mat, PlayerData data, String... otherUpgrades) {
		this.id = upgrade.getId();
		this.display = display;
		this.description = description;
		this.otherUpgrades = otherUpgrades;
		this.slot = slot;
		this.mat = mat;
		this.upgrade = upgrade;
		
		has = data.hasUpgrade(id);
		canGet = has || req.passesRequirement(data);
	}
	
	public void initialize(Session s, PlayerSessionData data) {
		upgrade.initialize(s, data);
	}
	
	public void onClick(Player p, PlayerData data) {
		if (canGet && !has) {
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			data.addUpgrade(this.upgrade);
			for (String upgrade : otherUpgrades) {
				data.addUpgrade(Upgrade.get(upgrade));
			}
		}
	}
	
	public ItemStack getIcon(PlayerData data) {
		ItemStack item = new ItemStack(canGet ? mat : Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		NamedTextColor c = data.hasUpgrade(id) ? NamedTextColor.GREEN : (canGet ? NamedTextColor.WHITE : NamedTextColor.RED);
		meta.displayName(Component.text(display + (has ? " (Owned)" : ""), c));
		meta.lore(SharedUtil.addLineBreaks(Component.text(description), 250));
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("id", id);
		return nbti.getItem();
	}
	
	public ItemStack updateItem(ItemStack item, PlayerData data) {
		has = data.hasUpgrade(id);
		canGet = has || req.passesRequirement(data);
		if (item.getType() == Material.BARRIER && canGet) {
			item = item.withType(mat);
		}
		ItemMeta meta = item.getItemMeta();
		NamedTextColor c = data.hasUpgrade(id) ? NamedTextColor.GREEN : (canGet ? NamedTextColor.WHITE : NamedTextColor.RED);
		meta.displayName(Component.text(display + (has ? " (Owned)" : ""), c));
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean passesRequirement(PlayerData data) {
		return req == null || req.passesRequirement(data);
	}
	
	public int getSlot() {
		return slot;
	}
}
