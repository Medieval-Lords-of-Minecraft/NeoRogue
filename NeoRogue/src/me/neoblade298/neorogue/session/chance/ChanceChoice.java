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
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceChoice {
	private Component title;
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
		this.title = SharedUtil.color(title);
	}
	
	public ItemStack getItem(Session s) {
		ItemStack item = new ItemStack(mat);
		
		// Check conditions
		boolean canRun = action != null ? action.run(s, false) : true;
		ItemMeta meta = item.getItemMeta();
		Component display = title;
		if (!canRun) {
			display = display.decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.RED);
		}
		ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
		meta.displayName(display);
		
		if (desc != null) {
			for (TextComponent text : desc) {
				if (!canRun) lore.add(text.decorate(TextDecoration.STRIKETHROUGH).decoration(TextDecoration.ITALIC, State.FALSE));
				else lore.add(text.decoration(TextDecoration.ITALIC, State.FALSE));
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
