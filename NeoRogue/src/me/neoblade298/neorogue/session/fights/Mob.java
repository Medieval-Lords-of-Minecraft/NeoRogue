package me.neoblade298.neorogue.session.fights;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.SkullUtil;
import me.neoblade298.neocore.shared.exceptions.NeoIOException;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import net.md_5.bungee.api.ChatColor;

public class Mob implements Comparable<Mob> {
	private static ArrayList<BuffType> typeOrder = new ArrayList<BuffType>();
	private static HashMap<String, Mob> mobs = new HashMap<String, Mob>();
	
	private String id, display, base64;
	private Material mat;
	private HashMap<BuffType, Integer> resistances = new HashMap<BuffType, Integer>();
	private HashMap<BuffType, Amount> damageTypes = new HashMap<BuffType, Amount>();
	private ArrayList<String> lore;
	
	static {
		typeOrder.add(BuffType.SLASHING);
		typeOrder.add(BuffType.PIERCING);
		typeOrder.add(BuffType.BLUNT);
		typeOrder.add(BuffType.FIRE);
		typeOrder.add(BuffType.ICE);
		typeOrder.add(BuffType.LIGHTNING);
		typeOrder.add(BuffType.EARTH);
		typeOrder.add(BuffType.LIGHT);
		typeOrder.add(BuffType.DARK);
		typeOrder.add(BuffType.BLEED);
		typeOrder.add(BuffType.POISON);
	}
	
	public static void load() throws NeoIOException {
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "mobs"), (yml, file) -> {
			for (String key : yml.getKeys(false)) {
				ConfigurationSection sec = yml.getConfigurationSection(key);
				Mob mob = new Mob(sec);
				mobs.put(key, mob);
			}
		});
	}
	
	public static Mob get(String id) {
		return mobs.get(id);
	}
	
	public Mob(ConfigurationSection sec) {
		id = sec.getName();
		Optional<MythicMob> opt = MythicBukkit.inst().getMobManager().getMythicMob(id);
		display = opt.isPresent() ? opt.get().getDisplayName().get() : "Mob Not Loaded";
		
		ConfigurationSection resSec = sec.getConfigurationSection("resistances");
		if (resSec != null) {
			for (String key : resSec.getKeys(false)) {
				int pct = resSec.getInt(key);
				if (key.equals("MAGICAL")) {
					resistances.put(BuffType.FIRE, pct);
					resistances.put(BuffType.ICE, pct);
					resistances.put(BuffType.LIGHTNING, pct);
					resistances.put(BuffType.EARTH, pct);
					resistances.put(BuffType.LIGHT, pct);
					resistances.put(BuffType.DARK, pct);
				}
				else if (key.equals("PHYSICAL")) {
					resistances.put(BuffType.SLASHING, pct);
					resistances.put(BuffType.PIERCING, pct);
					resistances.put(BuffType.BLUNT, pct);
				}
				else {
					resistances.put(BuffType.valueOf(key), pct);
				}
			}
		}
		
		ConfigurationSection dmgSec = sec.getConfigurationSection("BuffTypes");
		if (dmgSec != null) {
			for (String key : dmgSec.getKeys(false)) {
				damageTypes.put(BuffType.valueOf(key), Amount.valueOf(dmgSec.getString(key)));
			}
		}
		
		lore = SharedUtil.addLineBreaks(sec.getString("description"), 250, ChatColor.GRAY);
		mat = sec.contains("material") ? Material.valueOf(sec.getString("material")) : null;
		base64 = sec.getString("base64");
	}
	
	public HashMap<BuffType, Integer> getResistances() {
		return resistances;
	}
	
	public ItemStack getItemDisplay(ArrayList<MobModifier> modifiers) {
		ItemStack item = base64 == null ? new ItemStack(mat) : SkullUtil.itemFromBase64(base64);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(display);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§6Resistances:");
		for (BuffType dt : typeOrder) {
			if (resistances.containsKey(dt)) {
				int pct = resistances.get(dt);
				String str = (pct > 0 ? ChatColor.RED : ChatColor.GREEN) + "" + pct + "%";
				lore.add("§e" + dt.getDisplay() + "§7: " + str);
			}
		}
		
		for (BuffType dt : typeOrder) {
			if (damageTypes.containsKey(dt)) {
				lore.add("§e" + dt.getDisplay() + "§7: " + damageTypes.get(dt).getDisplay(true));
			}
		}
		
		lore.addAll(this.lore);
		meta.setLore(lore);
		item.setItemMeta(meta);
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
		return this.id.compareTo(o.id);
	}
	
	
}
