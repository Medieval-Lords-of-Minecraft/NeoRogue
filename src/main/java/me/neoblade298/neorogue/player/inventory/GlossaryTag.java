package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.DamageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public enum GlossaryTag implements GlossaryIcon {
	DIRECT(Material.DIAMOND_SWORD, "<#E8E8E8>Direct</#E8E8E8>",
			"A type of damage. Subtypes are <red>Physical</red>, <blue>Magical</blue>. Does not include status damage."),
	PHYSICAL(Material.DIAMOND_SWORD, "<#FF5555>Physical</#FF5555>",
			"A type of damage. Subtypes are " + DamageType.SLASHING.tag + ", " + DamageType.PIERCING.tag + ", " + DamageType.BLUNT.tag + "."),
	MAGICAL(Material.DIAMOND_SWORD, "<#5599FF>Magical</#5599FF>",
			"A type of damage. Subtypes are " + DamageType.FIRE.tag + ", " + DamageType.ICE.tag + ", " + DamageType.LIGHTNING.tag + ", "
			+ DamageType.EARTHEN.tag + ", " + DamageType.DARK.tag + ", " + DamageType.LIGHT.tag + "."),
	SLASHING(Material.STONE_SWORD, "<#E74C3C>Slashing</#E74C3C>",
			"A type of physical damage."),
	PIERCING(Material.TRIDENT, "<#FF6B6B>Piercing</#FF6B6B>",
			"A type of physical damage."),
	BLUNT(Material.STONE_AXE, "<#C96A4A>Blunt</#C96A4A>",
			"A type of physical damage."),
	FIRE(Material.BLAZE_POWDER, "<#FF6A00>Fire</#FF6A00>",
			"A type of magical damage."),
	ICE(Material.PACKED_ICE, "<#66D9FF>Ice</#66D9FF>",
			"A type of magical damage."),
	LIGHTNING(Material.LIGHTNING_ROD, "<#FFE14D>Lightning</#FFE14D>",
			"A type of magical damage."),
	EARTHEN(Material.DIRT, "<#9B7653>Earthen</#9B7653>",
			"A type of magical damage."),
	DARK(Material.OBSIDIAN, "<#6E44AA>Dark</#6E44AA>",
			"A type of magical damage."),
	LIGHT(Material.END_ROD, "<#FFF3B0>Light</#FFF3B0>",
			"A type of magical damage."),
	POISON(Material.GREEN_DYE, "<#55CC55>Poison</#55CC55>",
			"Deals poison damage per stack applied to the affected entity every second, ignoring shields, for its entire duration."),
	REND(Material.NETHERITE_SCRAP, "<#B84A62>Rend</#B84A62>",
			"Certain abilities become stronger with more stacks of these applied to enemies. 1 stack is removed every second."),
	BURN(Material.BLAZE_POWDER, "<#FF8C42>Burn</#FF8C42>",
			"Reduces the affected entity's fire resistance by damage by 50%. 1 stack is removed every second."),
	FROST(Material.PACKED_ICE, "<#7FDBFF>Frost</#7FDBFF>",
			"Reduces the affected entity's magical damage dealt by 25%. 1 stack is removed every second."),
	FROSTBITE(Material.BLUE_ICE, "<#3399FF>Frostbite</#3399FF>",
			"Reduces the affected entity's attack speed by 50%. If the affected entity uses a bow, it must be fully drawn."),
	IMPEDED(Material.COBWEB, "<#B8B8B8>Impeded</#B8B8B8>",
			"Reduces the affected entity's projectile velocity by 50%."),
	ELECTRIFIED(Material.LIGHTNING_ROD, "<#FFD700>Electrified</#FFD700>",
			"Whenever the affected entity casts an ability or deals damage, deal 5 lightning damage per stack applied to the affected entity. 1 stack is removed every second."),
	CONCUSSED(Material.DIRT, "<#C2A878>Concussed</#C2A878>",
			"Reduces the affected entity's physical damage dealt by 25%. 1 stack is removed every second."),
	INSANITY(Material.SOUL_SAND, "<#9B59B6>Insanity</#9B59B6>",
			"Increases the affected entity's magical damage taken by 50%. 1 stack is removed every second."),
	CORRUPTION(Material.FERMENTED_SPIDER_EYE, "<#7A1F5D>Corruption</#7A1F5D>",
			"Increases the affected entity's damage taken by 50%. Each time the affected entity receives damage, 1 stack is removed."),
	DAMPENED(Material.SPONGE, "<#D6C85F>Dampened</#D6C85F>",
			"While active, all power activations are cancelled."),
	SANCTIFIED(Material.END_ROD, "<#FFFACD>Sanctified</#FFFACD>",
			"Upon the affected entity receiving light damage, grant the attacker 5 shields for 5 seconds. 1 stack is removed every second."),
	THORNS(Material.DEAD_BUSH, "<#7B8D42>Thorns</#7B8D42>",
			"Upon the affected entity receiving physical damage, even if it is absorbed by shields, return 1 thorns damage as physical damage per stack."),
	WEAKENED(Material.FEATHER, "<#AAB7B8>Weakened</#AAB7B8>",
			"Reduces the affected entity's damage dealt by 50%."),
	WITHERED(Material.WITHER_ROSE, "<#4B5D3A>Withered</#4B5D3A>",
			"Prevents the affected entity from sprinting and jumping."),
	REFLECT(Material.GLASS_PANE, "<#8FE3FF>Reflect</#8FE3FF>",
			"Upon the affected entity receiving magical damage, even if it is absorbed by shields, return 1 reflect damage as magical damage per stack."),
	SHIELDS(Material.SHIELD, "<#FFFF55>Shields</#FFFF55>",
			"Absorbs damage before reaching your health post-mitigation. Some abilities ignore shields."),
	BARRIER(Material.SHIELD, "<#D9A441>Barrier</#D9A441>",
			"Intercepts projectiles, dealing the damage to you directly, usually with mitigation."),
	BERSERK(Material.BLAZE_POWDER, "<#DC143C>Berserk</#DC143C>",
			"Certain abilities become stronger upon reaching a certain threshold of these stacks."),
	TRAP(Material.OAK_TRAPDOOR, "<#4F86F7>Trap</#4F86F7>",
			"Placed at a set location. Other abilities may interact with these."),
	STRENGTH(Material.IRON_SWORD, "<#D64545>Strength</#D64545>",
			"Buffs all physical damage by 1 per stack."),
	INTELLECT(Material.BLAZE_ROD, "<#4B9CD3>Intellect</#4B9CD3>",
			"Buffs all magical damage by 2% per stack."),
	PROTECT(Material.TURTLE_HELMET, "<#7D9AA6>Protect</#7D9AA6>",
			"Buffs all physical defense by 1 per stack."),
	SHELL(Material.PRISMARINE_CRYSTALS, "<#40C4C4>Shell</#40C4C4>",
			"Buffs all magical defense by 1 per stack."),
	STEALTH(Material.NETHER_STAR, "<#6B6B8D>Stealth</#6B6B8D>",
			"Certain abilities become stronger when this status is applied."),
	EVADE(Material.PHANTOM_MEMBRANE, "<#58D68D>Evade</#58D68D>",
			"When the affected entity takes damage, post-buff damage dealt is first subtracted from stamina. One stack is lost per damage instance."),
	FOCUS(Material.SPYGLASS, "<#E6B800>Focus</#E6B800>",
			"Certain abilities become stronger upon reaching a certain threshold of these stacks."),
	RIFT(Material.MAGMA_CREAM, "<#8A2BE2>Rift</#8A2BE2>",
			"Placed at a set location. Other abilities may interact with these."),
	INJURY(Material.BONE, "<#A05252>Injury</#A05252>",
			"Increases the affected entity's physical damage taken by 50%. 1 stack is removed every second."),
	DASH(Material.WIND_CHARGE, "<#2ECC71>Dash</#2ECC71>",
			"You become invulnerable for 0.5s on dash. Certain abilities may trigger upon dashing."),
	CHARGE(Material.BLAZE_ROD, "<#F39C12>Charge</#F39C12>",
			"Become unable to jump and apply slowness based on level of charge to yourself. You cannot use other abilities during this time."),
	CHANNEL(Material.CLOCK, "<#C0392B>Channel</#C0392B>",
			"Become unable to move. You cannot use other abilities during this time."),
	POWER(Material.ENCHANTED_BOOK, "<#00CED1>Power</#00CED1>",
			"A one-time activation that grants a passive effect for the rest of the fight."),
	PASSIVE(Material.GLOW_INK_SAC, "<#9E9E9E>Passive</#9E9E9E>",
			"An effect that is always active for the entire fight and requires no activation.");
	
	private ItemStack icon;
	public String tag, lore;
	private ArrayList<TextComponent> loreComp;
	private Component ctag;
	private GlossaryTag(Material mat, String display, String lore) {
		icon = new ItemStack(mat);
		this.lore = whiteNumbers(lore);
		ItemMeta meta = icon.getItemMeta();
		this.tag = display;
		this.ctag = SharedUtil.color(this.tag).decoration(TextDecoration.ITALIC, State.FALSE);
		meta.displayName(ctag);
		this.loreComp = SharedUtil.addLineBreaks((TextComponent) SharedUtil.color(this.lore).colorIfAbsent(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
		meta.lore(loreComp);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		icon.setItemMeta(meta);
	}

	private static String whiteNumbers(String input) {
		Matcher matcher = PatternHolder.NUMERIC_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "<white>" + matcher.group(1) + "</white>");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	// Enum constants are initialized before static fields in this class body,
	// so use a holder to defer Pattern initialization until first use.
	private static class PatternHolder {
		private static final Pattern NUMERIC_PATTERN = Pattern.compile("(?<![\\w>])(-?\\d+(?:\\.\\d+)?(?:%|:[0-9]+)?)");
	}

	public Component getTag() {
		return ctag;
	}

	// For use in printing without line breaks (/nr glossary)
	public String getLoreString() {
		return lore;
	}

	public ArrayList<TextComponent> getLore() {
		return loreComp;
	}
	
	public String tag(Equipment eq) {
		eq.addTags(this);
		// If you ever want to nest tags within tags, add switch case here
		return this.tag;
	}

	public String tagPlural(Equipment eq) {
		eq.addTags(this);
		String prefix = this.tag.substring(0, this.tag.indexOf("</"));
		String suffix = this.tag.substring(this.tag.indexOf("</"));
		return prefix + "s" + suffix;
	}

	public String tag(Equipment eq, int amt, boolean upgradable) {
		eq.addTags(this);
		String color = upgradable ? "yellow" : "white";
		return "<" + color + ">" + amt + "</" + color + "> " + this.tag;
	}

	public String tag(Equipment eq, double amt, boolean upgradable) {
		eq.addTags(this);
		String color = upgradable ? "yellow" : "white";
		return "<" + color + ">" + amt + "</" + color + "> " + this.tag;
	}

	// Auto-colored variants: the value's yellow/white color is decided automatically by diffing the base
	// item against its upgraded counterpart (see DescUtil.val / Equipment.resolveUpgradeColors).
	public String tag(Equipment eq, int amt) {
		eq.addTags(this);
		return DescUtil.val(amt) + " " + this.tag;
	}

	public String tag(Equipment eq, double amt) {
		eq.addTags(this);
		return DescUtil.val(amt) + " " + this.tag;
	}
	
	@Override
	public String getId() {
		return this.name();
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
}
