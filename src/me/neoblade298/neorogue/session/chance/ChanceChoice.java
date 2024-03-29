package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceChoice {
	private Component title;
	private Material mat;
	private ArrayList<TextComponent> desc, reqFail;
	private TreeSet<GlossaryIcon> tags = new TreeSet<GlossaryIcon>(GlossaryIcon.comparator);
	private ChanceAction action;
	private ChanceRequirement req;
	
	public ChanceChoice(Material mat, String title, String description, String prereqFail, ChanceRequirement req, ChanceAction action) {
		this(mat, title, description, action);
		this.req = req;
		this.reqFail = SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().
				deserialize(prereqFail).colorIfAbsent(NamedTextColor.RED).decoration(TextDecoration.ITALIC, State.FALSE), 250);
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
		this.title = SharedUtil.color(title).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE)
				.colorIfAbsent(NamedTextColor.GOLD);
	}
	
	public TreeSet<GlossaryIcon> getTags() {
		return tags;
	}
	
	public void addTag(GlossaryIcon icon) {
		tags.add(icon);
	}
	
	// For use in glossary
	public ItemStack getItemWithoutConditions() {
		ItemStack item = new ItemStack(mat);
		
		// Check conditions
		ItemMeta meta = item.getItemMeta();
		Component display = title;
		meta.displayName(display);
		ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
		
		if (desc != null) {
			for (TextComponent text : desc) {
				text = (TextComponent) text.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE)
						.colorIfAbsent(NamedTextColor.GRAY);
				lore.add(text);
			}
		}
		if (!tags.isEmpty()) {
			lore.add((TextComponent) Component.text("Right click for more info", NamedTextColor.GRAY).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack getItem(Session s, PlayerSessionData data) {
		ItemStack item = getItemWithoutConditions();
		
		// Check conditions
		boolean canRun = req != null ? req.check(s, (ChanceInstance) s.getInstance(), data) : true;
		if (canRun) return item;
		
		// If conditions fail, add extra stuff
		ItemMeta meta = item.getItemMeta();
		meta.displayName(meta.displayName().decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.RED));
		ArrayList<Component> lore = new ArrayList<Component>();
		
		for (Component text : meta.lore()) {
			lore.add(text.decorate(TextDecoration.STRIKETHROUGH));
		}
		
		if (reqFail != null) {
			lore.addAll(reqFail);
		}
		meta.lore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean canChoose(Session s, ChanceInstance inst, PlayerSessionData data) {
		return req == null || req.check(s, inst, data);
	}
	
	public String choose(Session s, ChanceInstance inst, PlayerSessionData data) {
		if (!inst.getSet().isIndividual()) {
			s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
			for (Player p : s.getOnlinePlayers()) {
				p.closeInventory();
			}
		}
		else {
			Player p = data.getPlayer();
			Sounds.success.play(p, p, Audience.ORIGIN);
		}
		
		if (action != null) {
			return action.run(s, inst, data);
		}
		return null;
	}
}
