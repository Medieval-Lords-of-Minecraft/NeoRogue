package me.neoblade298.neorogue.session.fight;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.api.mobs.MythicMob;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.SkullUtil;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class Mob implements Comparable<Mob> {
	private static HashMap<String, Mob> mobs = new HashMap<String, Mob>();
	private static final Pattern glossaryPattern = Pattern.compile("%[a-zA-Z]+%");
	
	private MobType type;
	private String id, base64;
	private TextComponent display;
	private int amount;
	private double spawnValue, killValue, knockbackMultiplier;
	private Material mat;
	private HashMap<DamageCategory, Integer> resistances = new HashMap<DamageCategory, Integer>();
	private HashMap<DamageType, Amount> damageTypes = new HashMap<DamageType, Amount>();
	private TreeSet<GlossaryIcon> tags = new TreeSet<GlossaryIcon>(GlossaryIcon.comparator);
	private List<String> summons;
	private ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
	
	public static void load() {
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "mobs"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				Section sec = yml.getSection(key);
				Mob mob = new Mob(sec);
				mobs.put(key, mob);
			}
		});

		for (Mob mob : mobs.values()) {
			mob.resolveSpawnValue();
		}
	}
	
	public static Mob get(String id) {
		/**
		 * Don't do this, many mobs aren't in the Mob glossary (like BanditKingCondemn) and thus this will spam
		 * if (!mobs.containsKey(id)) {
		 *	Bukkit.getLogger().warning("[NeoRogue] Tried to get unregistered mob " + id);
		 *	return null;
		 * }
		 */
		return mobs.get(id);
	}

	public TextComponent getDisplay() {
		return display;
	}
	
	public Mob(Section sec) {
		id = sec.getName();
		Optional<MythicMob> opt = NeoRogue.mythicMobs.getMythicMob(id);
		display = Component.text(opt.isPresent() ? opt.get().getDisplayName().get() : "Mob Not Loaded");
		type = MobType.valueOf(sec.getString("type", "NORMAL").toUpperCase());
		knockbackMultiplier = sec.getDouble("knockback-multiplier", 1);
		
		Section resSec = sec.getSection("resistances");
		if (resSec != null) {
			for (String key : resSec.getKeys()) {
				int pct = resSec.getInt(key);
				DamageCategory cat = DamageCategory.valueOf(key);
				resistances.put(cat, pct);
				GlossaryTag tag = cat.toGlossary();
				if (tag != null) tags.add(tag);
			}
		}
		
		Section dmgSec = sec.getSection("damagetypes");
		if (dmgSec != null) {
			for (String key : dmgSec.getKeys()) {
				DamageType dt = DamageType.valueOf(key);
				damageTypes.put(dt, Amount.valueOf(dmgSec.getString(key)));
				GlossaryTag tag = dt.toGlossary();
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
		killValue = sec.getDouble("value", (double) 1 / (double) amount);
		spawnValue = killValue;
		summons = sec.getStringList("summons");
		base64 = sec.getString("base64");
	}
	
	public HashMap<DamageCategory, Integer> getResistances() {
		return resistances;
	}
	
	public TreeSet<GlossaryIcon> getTags() {
		return tags;
	}
	
	public MobType getType() {
		return type;
	}
	
	public double getKnockbackMultiplier() {
		return knockbackMultiplier;
	}
	
	public ItemStack getItemDisplay(ArrayList<MobModifier> modifiers) {
		ItemStack item = base64 == null ? new ItemStack(mat) : SkullUtil.fromBase64(base64);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(display);
		ArrayList<Component> lore = new ArrayList<Component>();
		Component value = Component.text("Value: ", NamedTextColor.GOLD).append(Component.text("" + this.spawnValue, NamedTextColor.YELLOW));
		lore.add(value.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		if (!resistances.isEmpty()) {
			Component header = Component.text("Resistances:", NamedTextColor.GOLD);
			lore.add(header.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
			for (DamageCategory cat : DamageCategory.values()) {
				if (resistances.containsKey(cat)) {
					int pct = resistances.get(cat);
					String sfx = pct > 0 ? "Resistant" : "Weak";
					Component c = Component.text(cat.getDisplay() + ": ", NamedTextColor.YELLOW)
							.append(Component.text(Math.abs(pct) + "% " + sfx, pct > 0 ? NamedTextColor.RED : NamedTextColor.GREEN));
					lore.add(c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
				}
			}
		}

		if (!damageTypes.isEmpty()) {
			Component header = Component.text("Damage:", NamedTextColor.GOLD);
			lore.add(header.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
			for (DamageType dt : DamageType.values()) {
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
	
	public double getSpawnValue() {
		return spawnValue;
	}
	
	public double getKillValue() {
		return killValue;
	}
	
	public void resolveSpawnValue() {
		if (summons == null) return;
		for (String summon : summons) {
			spawnValue += Mob.get(summon).getSpawnValue();
		}
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
