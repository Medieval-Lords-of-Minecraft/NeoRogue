package me.neoblade298.neorogue.player.inventory;

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
			"Deals 0.2 poison damage per stack applied to the holder every second, ignoring shields, for its entire duration. Anytime poison is " +
			"reapplied, the status duration is set to whichever is longer: the current status or the new applying source."),
	BLEED(Material.REDSTONE, StatusType.BLEED.tag,
			"Deals 0.2 bleed damage per stack applied to the holder and removes 1 stack every second, ignoring shields."),
	REND(Material.NETHERITE_SCRAP, StatusType.REND.tag,
			"Certain abilities become stronger with more stacks of these applied to enemies."),
	BURN(Material.BLAZE_POWDER, StatusType.BURN.tag,
			"Upon the holder taking damage, deals 0.2 fire damage per stack applied. 1 stack is removed every second."),
	FROST(Material.PACKED_ICE, StatusType.FROST.tag,
			"Decreases the holder's magic damage by 0.2 per stack. Upon the holder dealing magical damage, remove 25% of the stacks and " +
			"deal that much damage to the holder. 1 stack is removed every second."),
	ELECTRIFIED(Material.LIGHTNING_ROD, StatusType.ELECTRIFIED.tag,
			"Fires a projectile that deals 0.2 lightning damage per stack applied to the nearest enemy in a radius of 5. "
			+ "1 stack is removed and one projectile fired per second."),
	CONCUSSED(Material.DIRT, StatusType.CONCUSSED.tag,
			"Decreases the holder's physical damage by 0.2 per stack. Upon the holder dealing physical damage, remove 25% of the stacks and " +
			"deal that much Earthen damage to the holder. 1 stack is removed every second."),
	INSANITY(Material.SOUL_SAND, StatusType.INSANITY.tag,
			"Increases the holder's magic damage taken by 0.2 per stack. Remove 1 stack every second."),
	SANCTIFIED(Material.END_ROD, StatusType.SANCTIFIED.tag,
			"Upon the holder dealing damage, remove 25% of the stacks and " +
			"deal that much Light damage to the holder while healing that much to the damaged. 1 stack is removed every second."),
	THORNS(Material.DEAD_BUSH, StatusType.THORNS.tag,
			"Upon the holder receiving physical damage, even if it is absorbed by shields, return 1 thorns damage per stack."),
	REFLECT(Material.GLASS_PANE, StatusType.REFLECT.tag,
			"Upon the holder receiving magical damage, even if it is absorbed by shields, return 1 reflect damage per stack."),
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
			"Buffs all magical damage by 1 per stack."),
	PROTECT(Material.TURTLE_HELMET, StatusType.PROTECT.tag,
			"Buffs all physical defense by 1 per stack."),
	SHELL(Material.PRISMARINE_CRYSTALS, StatusType.SHELL.tag,
			"Buffs all magical defense by 1 per stack."),
	STEALTH(Material.NETHER_STAR, StatusType.STEALTH.tag,
			"Certain abilities become stronger when this status is applied."),
	EVADE(Material.PHANTOM_MEMBRANE, StatusType.EVADE.tag,
			"When the holder takes damage, damage dealt is first subtracted from stamina. One stack is lost per damage instance."),
	FOCUS(Material.SPYGLASS, StatusType.FOCUS.tag,
			"Certain abilities become stronger upon reaching a certain threshold of these stacks."),
	INJURY(Material.BONE, StatusType.INJURY.tag,
			"When the holder deals damage, a stack of injury is used to reduce 0.2 damage dealt post-buff until there are no stacks or no damage remaining. " +
			"1 stack is removed every second."),
	CHARGE(Material.BLAZE_ROD, "<gold>Charge</gold>",
			"Apply slowness <white>1</white> to yourself. You cannot use other abilities during this time."),
	CHANNEL(Material.CLOCK, "<red>Channel</red>",
			"Become unable to move. You cannot use other abilities during this time.");
	
	private ItemStack icon;
	public String tag;
	private Component ctag;
	private GlossaryTag(Material mat, String display, String lore) {
		icon = new ItemStack(mat);
		ItemMeta meta = icon.getItemMeta();
		this.tag = display;
		this.ctag = SharedUtil.color(display).decoration(TextDecoration.ITALIC, State.FALSE);
		meta.displayName(ctag);
		meta.lore(SharedUtil.addLineBreaks((TextComponent) SharedUtil.color(lore).colorIfAbsent(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE)));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		icon.setItemMeta(meta);
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
