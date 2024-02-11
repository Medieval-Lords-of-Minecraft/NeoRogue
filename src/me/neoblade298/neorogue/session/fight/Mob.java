package me.neoblade298.neorogue.session.fight;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.SkullUtil;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class Mob implements Comparable<Mob> {
	private static ArrayList<BuffType> typeOrder = new ArrayList<BuffType>();
	private static HashMap<String, Mob> mobs = new HashMap<String, Mob>();
	private static final Pattern glossaryPattern = Pattern.compile("%[a-zA-Z]+%");
	
	private MobType type;
	private String id, base64;
	private TextComponent display;
	private int amount;
	private double value;
	private Material mat;
	private HashMap<BuffType, Integer> resistances = new HashMap<BuffType, Integer>();
	private HashMap<BuffType, Amount> damageTypes = new HashMap<BuffType, Amount>();
	private HashSet<GlossaryTag> tags = new HashSet<GlossaryTag>();
	private List<String> summons;
	private ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
	
	static {
		typeOrder.add(BuffType.PHYSICAL);
		typeOrder.add(BuffType.MAGICAL);
		typeOrder.add(BuffType.SLASHING);
		typeOrder.add(BuffType.PIERCING);
		typeOrder.add(BuffType.BLUNT);
		typeOrder.add(BuffType.FIRE);
		typeOrder.add(BuffType.ICE);
		typeOrder.add(BuffType.LIGHTNING);
		typeOrder.add(BuffType.EARTHEN);
		typeOrder.add(BuffType.LIGHT);
		typeOrder.add(BuffType.DARK);
		typeOrder.add(BuffType.BLEED);
		typeOrder.add(BuffType.POISON);
	}
	
	public static void load() {
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "mobs"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				Section sec = yml.getSection(key);
				Mob mob = new Mob(sec);
				mobs.put(key, mob);
			}
		});
	}
	
	public static Mob get(String id) {
		return mobs.get(id);
	}
	
	public Mob(Section sec) {
		id = sec.getName();
		Optional<MythicMob> opt = NeoRogue.mythicMobs.getMythicMob(id);
		display = Component.text(opt.isPresent() ? opt.get().getDisplayName().get() : "Mob Not Loaded");
		type = MobType.valueOf(sec.getString("type", "NORMAL").toUpperCase());
		
		Section resSec = sec.getSection("resistances");
		if (resSec != null) {
			for (String key : resSec.getKeys()) {
				int pct = resSec.getInt(key);
				BuffType bt = BuffType.valueOf(key);
				resistances.put(bt, pct);
				GlossaryTag tag = BuffType.toGlossary(bt);
				if (tag != null) tags.add(tag);
			}
		}
		
		Section dmgSec = sec.getSection("damagetypes");
		if (dmgSec != null) {
			for (String key : dmgSec.getKeys()) {
				BuffType bt = BuffType.valueOf(key);
				damageTypes.put(bt, Amount.valueOf(dmgSec.getString(key)));
				GlossaryTag tag = BuffType.toGlossary(bt);
				if (tag != null) tags.add(tag);
			}
		}
		
		String desc = sec.getString("description");
		StringBuilder sb = new StringBuilder();
		Matcher m = glossaryPattern.matcher(desc);
		while (m.find()) {
			String toParse = m.group();
			if (toParse.length() <= 2) continue;
			toParse = toParse.substring(1, toParse.length() - 1).toUpperCase();
			try {
				GlossaryTag tag = GlossaryTag.valueOf(toParse);
				tags.add(tag);
				m.appendReplacement(sb, tag.tag);
			}
			catch (IllegalArgumentException ex) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to parse mob glossary tag " + toParse + " for mob " + id);
				continue;
			}
		}
		m.appendTail(sb);
		desc = sb.toString();
		
		ArrayList<TextComponent> italicizedLore = SharedUtil.addLineBreaks(
				(TextComponent) SharedUtil.color(desc).colorIfAbsent(NamedTextColor.GRAY), 250);
		for (TextComponent tc : italicizedLore) {
			lore.add((TextComponent) tc.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		mat = sec.contains("material") ? Material.valueOf(sec.getString("material")) : null;
		amount = sec.getInt("amount", 1);
		value = sec.getDouble("value", (double) 1 / (double) amount);
		summons = sec.getStringList("summons");

		if (opt.isPresent() && opt.get().getEntityType() == MythicEntityType.SLIME) value /= 2; // Slime's death event is called twice so this must happen
		base64 = sec.getString("base64");
	}
	
	public HashMap<BuffType, Integer> getResistances() {
		return resistances;
	}
	
	public HashSet<GlossaryTag> getTags() {
		return tags;
	}
	
	public MobType getType() {
		return type;
	}
	
	public ItemStack getItemDisplay(ArrayList<MobModifier> modifiers) {
		ItemStack item = base64 == null ? new ItemStack(mat) : SkullUtil.itemFromBase64(base64);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(display);
		ArrayList<Component> lore = new ArrayList<Component>();
		if (!resistances.isEmpty()) {
			Component header = Component.text("Resistances:", NamedTextColor.GOLD);
			lore.add(header.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
			for (BuffType dt : typeOrder) {
				if (resistances.containsKey(dt)) {
					int pct = resistances.get(dt);
					Component c = Component.text(dt.getDisplay() + ": ", NamedTextColor.YELLOW)
							.append(Component.text(pct + "%", pct > 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
					lore.add(c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
				}
			}
		}

		if (!damageTypes.isEmpty()) {
			Component header = Component.text("Damage:", NamedTextColor.GOLD);
			lore.add(header.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
			for (BuffType dt : typeOrder) {
				if (damageTypes.containsKey(dt)) {
					Component c = Component.text(dt.getDisplay(), NamedTextColor.YELLOW)
							.append(Component.text(": ", NamedTextColor.GRAY))
							.append(damageTypes.get(dt).getDisplay(true));
					lore.add(c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
				}
			}
		}
		
		lore.addAll(this.lore);
		meta.lore(lore);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("mobId", id);
		return nbti.getItem();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Mob other = (Mob) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}

	@Override
	public int compareTo(Mob o) {
		return this.id.compareTo(o.id);
	}
	
	public double getValue() {
		return value;
	}

	public int getAmount() {
		return amount;
	}
	
	public List<String> getSummons() {
		return summons;
	}
	
	public String getId() {
		return id;
	}
	
	public static enum MobType {
		NORMAL,
		MINIBOSS,
		BOSS;
	}
	
	public enum Amount {
		NONE(NamedTextColor.GRAY, "None"),
		LIGHT(NamedTextColor.YELLOW, "Light"),
		MEDIUM(NamedTextColor.GOLD, "Medium"),
		HEAVY(NamedTextColor.RED, "Heavy");
		
		private NamedTextColor color;
		private String display;
		private Amount(NamedTextColor color, String display) {
			this.color = color;
			this.display = display;
		}
		
		public Component getDisplay(boolean hasColor) {
			Component c = Component.text(display);
			return hasColor ? c.color(color) : c;
		}
	}
}
