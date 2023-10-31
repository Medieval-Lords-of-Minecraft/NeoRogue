package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChanceChoice {
	private String title;
	private Material mat;
	private ChanceStage result;
	private ArrayList<TextComponent> desc, prereqFail;
	private ChanceAction action;
	
	public ChanceChoice(Material mat, String title, String description, String prereqFail, ChanceAction action) {
		this(mat, title, description, action);
		this.prereqFail = SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().deserialize(prereqFail), 250);
	}
	
	public ChanceChoice(Material mat, String title, String description, ChanceAction action) {
		this(mat, title, description);
		this.action = action;
	}
	
	public ChanceChoice(Material mat, String title, String description) {
		this(mat, title);
		this.desc = SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().deserialize(description), 250);
	}
	
	public ChanceChoice(Material mat, String title) {
		this.mat = mat;
		this.title = title;
	}
	
	public ItemStack getItem(Session s) {
		ItemStack item = new ItemStack(mat);
		
		// Check conditions
		boolean canRun = action != null ? action.run(s, false) : true;
		ItemMeta meta = item.getItemMeta();
		Component display = Component.text(title, canRun ? NamedTextColor.YELLOW : NamedTextColor.RED);
		if (!canRun) display.decorate(TextDecoration.STRIKETHROUGH);
		ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
		
		if (desc != null) {
			for (TextComponent text : desc) {
				if (!canRun) text.decorate(TextDecoration.STRIKETHROUGH);
			}
		}
		
		if (!canRun && prereqFail != null) {
			lore.addAll(prereqFail);
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void setResult(ChanceStage stage) {
		this.result = stage;
	}
	
	public ChanceStage choose(Session s, ChanceInstance inst) {
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		for (Player player : s.getOnlinePlayers()) {
			player.closeInventory();
		}
		if (action != null) action.run(s, true);
		return result;
	}
}
