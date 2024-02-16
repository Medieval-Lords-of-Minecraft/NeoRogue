package me.neoblade298.neorogue;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class Placeholders extends PlaceholderExpansion {
	
    @Override
    public boolean canRegister(){
        return Bukkit.getPluginManager().getPlugin("NeoRogue") != null;
    }
    
    @Override
    public boolean register(){
    	if (!canRegister()) return false;
    	return super.register();
    }

	@Override
	public String getAuthor() {
		return "Ascheladd";
	}
	
    @Override
    public boolean persist(){
        return true;
    }

	@Override
	public String getIdentifier() {
		return "nrboard";
	}

    @Override
    public String getRequiredPlugin(){
        return "NeoRogue";
    }
    
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		if (p == null) return "N/A";
		if (identifier.length() > 1) return "Placeholder error A";
		PlayerData data = PlayerManager.getPlayerData(p.getUniqueId());
		if (data == null) return "Loading...";
		int i = Integer.parseInt(identifier);
		ArrayList<String> lines = data.getBoardLines();
		if (lines == null) return "";
		return lines.size() > i ? lines.get(i) : "-";
	}
}
