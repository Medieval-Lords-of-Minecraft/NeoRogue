package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.modifier.Alacrity;
import me.neoblade298.neorogue.session.fight.modifier.Beefy;
import me.neoblade298.neorogue.session.fight.modifier.IronWall;
import me.neoblade298.neorogue.session.fight.modifier.ManaBarrier;
import me.neoblade298.neorogue.session.fight.modifier.Martyr;
import me.neoblade298.neorogue.session.fight.modifier.Sharpshooter;
import me.neoblade298.neorogue.session.fight.modifier.Windcutter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

/**
 * A modifier applied to a non-normal (miniboss/boss) mob when the
 * {@link me.neoblade298.neorogue.session.settings.NotorietySetting#MOB_MODIFIERS} notoriety
 * setting is active. Each modifier is a singleton registered in {@link #registry} and referenced by
 * {@link #id} for serialization. The concrete behavior is attached to the mob's {@link FightData} in
 * {@link #initialize(FightData)}, which the fight instance calls when the modified mob spawns.
 *
 * <p>To add a new modifier, extend this class, provide an id/title/description, implement
 * {@link #initialize(FightData)}, and register the singleton in {@link #registerModifiers()}.
 */
public abstract class MobModifier {
	// Registry of every modifier singleton, keyed by id. Insertion-ordered for stable random selection.
	private static final LinkedHashMap<String, MobModifier> registry = new LinkedHashMap<String, MobModifier>();

	protected final String id;
	protected final TextComponent title;
	protected final ArrayList<TextComponent> description;
	protected final boolean isBossModifier; // Whether this modifier is exclusive to miniboss and boss

	protected MobModifier(String id, TextComponent title, TextComponent description, boolean isBossModifier) {
		this.id = id;
		this.title = title;
		this.description = SharedUtil.addLineBreaks(description, 200);
		this.isBossModifier = isBossModifier;
	}

	// Registers all built-in modifiers. Called once at startup (and on reload).
	public static void registerModifiers() {
		registry.clear();
		register(new Alacrity());
		register(new Beefy());
		register(new Martyr());
		register(new Windcutter());
		register(new IronWall());
		register(new ManaBarrier());
		register(new Sharpshooter());
	}

	protected static void register(MobModifier mod) {
		registry.put(mod.id, mod);
	}

	// Looks up a modifier singleton by id. Used when deserializing a saved fight instance.
	public static MobModifier get(String id) {
		return registry.get(id);
	}

	// Picks a random registered modifier, or null if none qualify. When allowBossModifiers is false,
	// modifiers flagged as boss-only are excluded (used by standard fights).
	public static MobModifier generate(boolean allowBossModifiers) {
		ArrayList<MobModifier> mods = new ArrayList<MobModifier>();
		for (MobModifier mod : registry.values()) {
			if (!allowBossModifiers && mod.isBossModifier) continue;
			mods.add(mod);
		}
		if (mods.isEmpty()) return null;
		return mods.get(NeoRogue.gen.nextInt(mods.size()));
	}

	// Called when the modified mob's FightData is created. Attach triggers/effects to the mob here.
	public abstract void initialize(FightData mob);

	public String getId() {
		return id;
	}

	public TextComponent getTitle() {
		return title;
	}

	public ArrayList<TextComponent> getDescription() {
		return description;
	}

	public boolean isBossModifier() {
		return isBossModifier;
	}

	// Appends the modifier's title and description to an item's lore for the Fight Info inventory.
	public void appendDisplay(ArrayList<Component> lore) {
		lore.add(Component.empty());
		lore.add(Component.text("Modifier: ", NamedTextColor.LIGHT_PURPLE).append(title)
				.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		for (TextComponent line : description) {
			lore.add(line.colorIfAbsent(NamedTextColor.GRAY).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
	}

	// Builds the standalone item shown for this modifier in the Fight Info inventory.
	public ItemStack getIcon() {
		ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Modifier: ", NamedTextColor.LIGHT_PURPLE).append(title)
				.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> lore = new ArrayList<Component>();
		for (TextComponent line : description) {
			lore.add(line.colorIfAbsent(NamedTextColor.GRAY).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
