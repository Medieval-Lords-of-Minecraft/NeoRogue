package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public enum GlossaryTag implements GlossaryIcon {
	GENERAL(Material.DIAMOND_SWORD, "<white>General</white>",
			"A type of damage. Subtypes are <red>Physical</red>, <blue>Magical</blue>. Does not include status damage."),
	PHYSICAL(Material.DIAMOND_SWORD, "<red>Physical</red>",
			"A type of damage. Subtypes are " + DamageType.SLASHING.tag + ", " + DamageType.PIERCING.tag + ", " + DamageType.BLUNT.tag + "."),
	MAGICAL(Material.DIAMOND_SWORD, "<blue>Magical</blue>",
			"A type of damage. Subtypes are " + DamageType.FIRE.tag + ", " + DamageType.ICE.tag + ", " + DamageType.LIGHTNING.tag + ", "
			+ DamageType.EARTHEN.tag + ", " + DamageType.DARK.tag + ", " + DamageType.LIGHT.tag + "."),
	SLASHING(Material.STONE_SWORD, DamageType.SLASHING.tag,
			"A type of physical damage."),
	PIERCING(Material.TRIDENT, DamageType.PIERCING.tag,
			"A type of physical damage."),
	BLUNT(Material.STONE_AXE, DamageType.BLUNT.tag,
			"A type of physical damage."),
	FIRE(Material.BLAZE_POWDER, DamageType.FIRE.tag,
			"A type of magical damage."),
	ICE(Material.PACKED_ICE, DamageType.ICE.tag,
			"A type of magical damage."),
	LIGHTNING(Material.LIGHTNING_ROD, DamageType.LIGHTNING.tag,
			"A type of magical damage."),
	EARTHEN(Material.DIRT, DamageType.EARTHEN.tag,
			"A type of magical damage."),
	DARK(Material.OBSIDIAN, DamageType.DARK.tag,
			"A type of magical damage."),
	LIGHT(Material.END_ROD, DamageType.LIGHT.tag,
			"A type of magical damage."),
	POISON(Material.GREEN_DYE, StatusType.POISON.tag,
			"Deals 0.2 poison damage per stack applied to the affected entity every second, ignoring shields, for its entire duration. 1 stack is removed every tick."),
	REND(Material.NETHERITE_SCRAP, StatusType.REND.tag,
			"Certain abilities become stronger with more stacks of these applied to enemies. 1 stack is removed every tick."),
	BURN(Material.BLAZE_POWDER, StatusType.BURN.tag,
			"Reduces the affected entity's fire resistance by damage by 50%. 1 stack is removed every tick."),
	FROST(Material.PACKED_ICE, StatusType.FROST.tag,
			"Decreases the affected entity's magic damage by 0.2 per stack. Upon the affected entity dealing magical damage, remove 25% of the stacks. " +
			"1 stack is removed every tick."),
	ELECTRIFIED(Material.LIGHTNING_ROD, StatusType.ELECTRIFIED.tag,
			"Whenever the affected entity casts an ability or deals damage, deal 5 lightning damage per stack applied to the affected entity. 1 stack is removed every tick."),
	CONCUSSED(Material.DIRT, StatusType.CONCUSSED.tag,
			"Decreases the affected entity's physical damage by 0.2 per stack. Upon the affected entity dealing physical damage, remove 25% of the stacks. " +
			"1 stack is removed every tick."),
	INSANITY(Material.SOUL_SAND, StatusType.INSANITY.tag,
			"Increases the affected entity's magic damage taken by 0.2 per stack. 1 stack is removed every tick."),
	CORRUPTION(Material.FERMENTED_SPIDER_EYE, StatusType.CORRUPTION.tag,
			"Increases the affected entity's damage taken by 50%. Each time the affected entity receives damage, 1 stack is removed."),
	SANCTIFIED(Material.END_ROD, StatusType.SANCTIFIED.tag,
			"Upon the affected entity receiving light damage, grant the attacker 0.1 shields per stack for 6 seconds. 1 stack is removed every tick."),
	THORNS(Material.DEAD_BUSH, StatusType.THORNS.tag,
			"Upon the affected entity receiving physical damage, even if it is absorbed by shields, return 1 thorns damage per stack."),
	REFLECT(Material.GLASS_PANE, StatusType.REFLECT.tag,
			"Upon the affected entity receiving magical damage, even if it is absorbed by shields, return 1 reflect damage per stack."),
	SHIELDS(Material.SHIELD, "<yellow>Shields</yellow>",
			"Absorbs damage before reaching your health post-mitigation. Some abilities ignore shields."),
	BARRIER(Material.SHIELD, "<gold>Barrier</gold>",
			"Intercepts projectiles, dealing the damage to you directly, usually with mitigation."),
	THREATEN(Material.REDSTONE_TORCH, "<dark_red>Threaten</dark_red>",
			"Increases your threat towards an enemy. Enemies prioritize players with the highest threat. Dealing damage also increases threat at a 1:1 ratio post-mitigation."),
	BERSERK(Material.BLAZE_POWDER, StatusType.BERSERK.tag,
			"Certain abilities become stronger upon reaching a certain threshold of these stacks."),
	TRAP(Material.OAK_TRAPDOOR, "<blue>Trap</blue>",
			"Placed at a set location. Other abilities may interact with these."),
	STRENGTH(Material.IRON_SWORD, StatusType.STRENGTH.tag,
			"Buffs all physical damage by 1 per stack."),
	INTELLECT(Material.BLAZE_ROD, StatusType.INTELLECT.tag,
			"Buffs all magical damage by 2% per stack."),
	PROTECT(Material.TURTLE_HELMET, StatusType.PROTECT.tag,
			"Buffs all physical defense by 1 per stack."),
	SHELL(Material.PRISMARINE_CRYSTALS, StatusType.SHELL.tag,
			"Buffs all magical defense by 1 per stack."),
	STEALTH(Material.NETHER_STAR, StatusType.STEALTH.tag,
			"Certain abilities become stronger when this status is applied."),
	EVADE(Material.PHANTOM_MEMBRANE, StatusType.EVADE.tag,
			"When the affected entity takes damage, post-buff damage dealt is first subtracted from stamina. One stack is lost per damage instance."),
	FOCUS(Material.SPYGLASS, StatusType.FOCUS.tag,
			"Certain abilities become stronger upon reaching a certain threshold of these stacks."),
	RIFT(Material.MAGMA_CREAM, "<dark_purple>Rift</dark_purple>",
			"Placed at a set location. Other abilities may interact with these."),
	INJURY(Material.BONE, StatusType.INJURY.tag,
			"When the affected entity deals damage, a stack of injury is used to reduce 0.2 damage dealt post-buff until there are no stacks or no damage remaining. " +
			"1 stack is removed every tick."),
	DASH(Material.WIND_CHARGE, "<green>Dash</green>",
			"You become invulnerable for 0.5s on dash. Certain abilities may trigger upon dashing."),
	CHARGE(Material.BLAZE_ROD, "<gold>Charge</gold>",
			"Become unable to jump and apply slowness based on level of charge to yourself. You cannot use other abilities during this time."),
	CHANNEL(Material.CLOCK, "<red>Channel</red>",
			"Become unable to move. You cannot use other abilities during this time.");
	
	private ItemStack icon;
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("(?<![\\w>])(-?\\d+(?:\\.\\d+)?(?:%|:[0-9]+)?)");
	public String tag, lore;
	private ArrayList<TextComponent> loreComp;
	private Component ctag;
	private GlossaryTag(Material mat, String display, String lore) {
		icon = new ItemStack(mat);
		this.lore = whiteNumbers(lore);
		ItemMeta meta = icon.getItemMeta();
		this.tag = display;
		this.ctag = SharedUtil.color(display).decoration(TextDecoration.ITALIC, State.FALSE);
		meta.displayName(ctag);
		this.loreComp = SharedUtil.addLineBreaks((TextComponent) SharedUtil.color(this.lore).colorIfAbsent(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
		meta.lore(loreComp);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		icon.setItemMeta(meta);
	}

	private static String whiteNumbers(String input) {
		Matcher matcher = NUMERIC_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "<white>" + matcher.group(1) + "</white>");
		}
		matcher.appendTail(sb);
		return sb.toString();
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
	
	@Override
	public String getId() {
		return this.name();
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
}
