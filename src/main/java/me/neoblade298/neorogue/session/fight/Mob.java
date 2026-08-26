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

import de.tr7zw.nbtapi.NBT;
import io.lumine.mythic.api.mobs.MythicMob;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.SkullUtil;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class Mob implements Comparable<Mob> {
	private static HashMap<String, Mob> mobs = new HashMap<String, Mob>();
	private static final Pattern glossaryPattern = Pattern.compile("%[a-zA-Z]+%");
	
	private MobType type;
	private String id, statId, base64;
	private TextComponent display;
	private double baseHealth;
	private int amount;
	private double spawnValue, killValue, knockbackMultiplier;
	private Material mat;
	private HashMap<DamageCategory, Integer> resistances = new HashMap<DamageCategory, Integer>();
	private HashMap<DamageType, Amount> damageTypes = new HashMap<DamageType, Amount>();
	private HashMap<DamageCategory, Amount> damageCategories = new HashMap<DamageCategory, Amount>();
	private TreeSet<GlossaryIcon> tags = new TreeSet<GlossaryIcon>(GlossaryIcon.comparator);
	private List<String> summons, disabledModifiers;
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
	
	// Distinct canonical stat ids of every registered mob, used to tab-complete /nrlytics mob (which
	// queries analytics keyed by stat id). Sorted for a stable, readable completion list.
	public static TreeSet<String> getStatIds() {
		TreeSet<String> ids = new TreeSet<String>();
		for (Mob mob : mobs.values()) {
			ids.add(mob.statId);
		}
		return ids;
	}

	public static TreeSet<String> getStatIds(MobType type) {
		TreeSet<String> ids = new TreeSet<String>();
		for (Mob mob : mobs.values()) {
			if (mob.type == type) ids.add(mob.statId);
		}
		return ids;
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

	// Returns the canonical stat id for a mob, collapsing alternate "forms" (e.g. Angvoth2 -> Angvoth)
	// so their statistics are combined into one entity. Falls back to the id itself when the mob isn't
	// registered or declares no alias.
	public static String getStatId(String id) {
		if (id == null) return null;
		Mob m = mobs.get(id);
		return m != null ? m.statId : id;
	}

	public String getStatId() {
		return statId;
	}

	public TextComponent getDisplay() {
		return display;
	}
	
	public Mob(Section sec) {
		id = sec.getName();
		// Alternate forms of the same entity can share stats by pointing at a canonical id
		statId = sec.getString("stat-alias", id);
		Optional<MythicMob> opt = NeoRogue.mythicMobs.getMythicMob(id);
		if (opt.isPresent()) {
			MythicMob mm = opt.get();
			display = Component.text(mm.getDisplayName().isPresent() ? mm.getDisplayName().get() : "Mob Not Loaded");
			baseHealth = mm.getHealth().get();
		}
		else {
			display = Component.text("Mob Not Loaded");
			baseHealth = 0;
		}
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
				Amount amount = Amount.valueOf(dmgSec.getString(key));
				GlossaryTag tag;
				try {
					DamageType dt = DamageType.valueOf(key);
					damageTypes.put(dt, amount);
					tag = dt.toGlossary();
				} catch (IllegalArgumentException ex) {
					DamageCategory category = DamageCategory.valueOf(key);
					damageCategories.put(category, amount);
					tag = category.toGlossary();
				}
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
		disabledModifiers = sec.getStringList("disabled-modifiers");
		base64 = sec.getString("base64");
	}

	public double getMaxHealthScale(Session s) {
		return getMaxHealthScale(s, s.getLevel());
	}

	public double getMaxHealthScale(Session s, int lvl) {
		double mhealth = baseHealth;
		double scale = 1.0;
		if (type != MobType.NORMAL) {
			scale += (s.getRegionsCompleted() * 1); // 100% health increase per region completed
			mhealth *= (s.getParty().size() + 1) * 0.5; // 50% health scale per player
		}
		scale += lvl * 0.1; // Base 10%
		mhealth *= scale;
		if (NotorietySetting.INCREASE_HEALTH.isActive(s)) {
			mhealth *= NotorietySetting.INCREASE_HEALTH_MULTIPLIER;
		}

		double result = Math.round(mhealth / 5.0) * 5.0;
		if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] getMaxHealthScale: mob=" + id
				+ " baseHealth=" + baseHealth + " lvl=" + lvl + " (sessionLevel=" + s.getLevel() + ")"
				+ " regionsCompleted=" + s.getRegionsCompleted() + " partySize=" + s.getParty().size()
				+ " scale=" + scale + " result=" + result);
		return result;
	}

	public double getBaseHealth() {
		return baseHealth;
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
	
	public ItemStack getItemDisplay(Session s, FightInstance inst, MobModifier modifier, boolean isChance) {
		ItemStack item = base64 == null ? new ItemStack(mat) : SkullUtil.fromBase64(base64);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(display);
		ArrayList<Component> lore = new ArrayList<Component>();
		int effectiveLevel = isChance ? s.getLevel() : s.getLevel() + 1;

		// Add 1 to session level to show next node's health if it's next node. If it's a chance, don't
		Component stats = Component.text("Health: ", NamedTextColor.GOLD)
				.append(Component.text("" + (int) getMaxHealthScale(s, effectiveLevel), NamedTextColor.YELLOW));
		if (inst instanceof StandardFightInstance) {
			stats = stats.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
					.append(Component.text("Fight Progress: ", NamedTextColor.GOLD))
					.append(Component.text("+" + (int) spawnValue, NamedTextColor.YELLOW));
		}
		lore.add(stats.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));

		if (!damageTypes.isEmpty() || !damageCategories.isEmpty()) {
			Component damage = Component.text("Damage: ", NamedTextColor.GOLD);
			boolean first = true;
			for (DamageType dt : DamageType.values()) {
				if (!damageTypes.containsKey(dt)) continue;
				if (!first) damage = damage.append(Component.text(", ", NamedTextColor.GRAY));
				damage = damage.append(Component.text(dt.getDisplay() + " ", NamedTextColor.YELLOW))
						.append(damageTypes.get(dt).getDisplay(true));
				first = false;
			}
			for (DamageCategory category : DamageCategory.values()) {
				if (!damageCategories.containsKey(category)) continue;
				if (!first) damage = damage.append(Component.text(", ", NamedTextColor.GRAY));
				damage = damage.append(Component.text(category.getDisplay() + " ", NamedTextColor.YELLOW))
						.append(damageCategories.get(category).getDisplay(true));
				first = false;
			}
			lore.add(damage.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}

		Component resistant = Component.text("Resists: ", NamedTextColor.GOLD);
		Component weak = Component.text("Weaknesses: ", NamedTextColor.GOLD);
		boolean firstResistance = true;
		boolean firstWeakness = true;
		for (DamageCategory cat : DamageCategory.values()) {
			if (!resistances.containsKey(cat)) continue;
			int pct = resistances.get(cat);
			if (pct > 0) {
				if (!firstResistance) resistant = resistant.append(Component.text(", ", NamedTextColor.GRAY));
				resistant = resistant.append(Component.text(cat.getDisplay() + " " + pct + "%", NamedTextColor.RED));
				firstResistance = false;
			}
			else {
				if (!firstWeakness) weak = weak.append(Component.text(", ", NamedTextColor.GRAY));
				weak = weak.append(Component.text(cat.getDisplay() + " " + Math.abs(pct) + "%", NamedTextColor.GREEN));
				firstWeakness = false;
			}
		}
		if (!firstResistance) lore.add(resistant.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		if (!firstWeakness) lore.add(weak.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		
		lore.addAll(this.lore);
		if (modifier != null) {
			modifier.appendDisplay(lore);
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		NBT.modify(item, nbt -> { nbt.setString("mobId", id); });
		return item;
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
		// Sort bosses first, then minibosses, then normal mobs
		int typeCompare = Integer.compare(o.type.ordinal(), this.type.ordinal());
		if (typeCompare != 0) return typeCompare;
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
			if (Mob.get(summon) == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to resolve spawn value for mob " + id + " because summon " + summon + " doesn't exist!");
				continue;
			}
			spawnValue += Mob.get(summon).getSpawnValue();
		}
	}

	public int getAmount() {
		return amount;
	}
	
	public List<String> getSummons() {
		return summons;
	}

	public List<String> getDisabledModifiers() {
		return disabledModifiers;
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
		NONE(NamedTextColor.GRAY, "◇◇◇"),
		LIGHT(NamedTextColor.YELLOW, "◆◇◇"),
		MEDIUM(NamedTextColor.GOLD, "◆◆◇"),
		HEAVY(NamedTextColor.RED, "◆◆◆");
		
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
