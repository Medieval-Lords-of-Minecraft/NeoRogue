package me.neoblade298.neorogue.session.fights;

import java.io.File;
import java.text.DecimalFormat;
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
import me.neoblade298.neocore.shared.exceptions.NeoIOException;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.map.MapPiece;
import net.md_5.bungee.api.ChatColor;

public class Mob implements Comparable<Mob> {
	private static ArrayList<DamageType> typeOrder = new ArrayList<DamageType>();
	private static HashMap<String, Mob> mobs = new HashMap<String, Mob>();
	
	private String id, display;
	private Material mat;
	private HashMap<DamageType, Integer> resistances = new HashMap<DamageType, Integer>();
	private HashMap<DamageType, Amount> damageTypes = new HashMap<DamageType, Amount>();
	private ArrayList<String> lore;
	
	static {
		typeOrder.add(DamageType.SLASHING);
		typeOrder.add(DamageType.PIERCING);
		typeOrder.add(DamageType.BLUNT);
		typeOrder.add(DamageType.FIRE);
		typeOrder.add(DamageType.ICE);
		typeOrder.add(DamageType.LIGHTNING);
		typeOrder.add(DamageType.EARTH);
		typeOrder.add(DamageType.LIGHT);
		typeOrder.add(DamageType.DARK);
		typeOrder.add(DamageType.BLEED);
		typeOrder.add(DamageType.POISON);
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
					resistances.put(DamageType.FIRE, pct);
					resistances.put(DamageType.ICE, pct);
					resistances.put(DamageType.LIGHTNING, pct);
					resistances.put(DamageType.EARTH, pct);
					resistances.put(DamageType.LIGHT, pct);
					resistances.put(DamageType.DARK, pct);
				}
				else if (key.equals("PHYSICAL")) {
					resistances.put(DamageType.SLASHING, pct);
					resistances.put(DamageType.PIERCING, pct);
					resistances.put(DamageType.BLUNT, pct);
				}
				else {
					resistances.put(DamageType.valueOf(key), pct);
				}
			}
		}
		
		ConfigurationSection dmgSec = sec.getConfigurationSection("damageTypes");
		if (dmgSec != null) {
			for (String key : dmgSec.getKeys(false)) {
				damageTypes.put(DamageType.valueOf(key), Amount.valueOf(dmgSec.getString(key)));
			}
		}
		
		lore = SharedUtil.addLineBreaks(sec.getString("description"), 250, ChatColor.GRAY);
		mat = Material.valueOf(sec.getString("material"));
	}
	
	public ItemStack getItemDisplay(ArrayList<MobModifier> modifiers) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(display);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§6Resistances:");
		for (DamageType dt : typeOrder) {
			if (resistances.containsKey(dt)) {
				int pct = resistances.get(dt);
				String str = (pct > 0 ? ChatColor.RED : ChatColor.GREEN) + "" + pct + "%";
				lore.add("§e" + dt.getDisplay() + "§7: " + str);
			}
		}
		
		for (DamageType dt : typeOrder) {
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
