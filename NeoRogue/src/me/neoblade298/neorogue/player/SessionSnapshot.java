package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.Session;

public class SessionSnapshot {
	private long lastSaved;
	private int nodesVisited;
	private AreaType areaType;
	private HashMap<String, PlayerClass> party = new HashMap<String, PlayerClass>();
	private HashSet<UUID> partyIds = new HashSet<UUID>();
	
	public SessionSnapshot(Session s) {
		this.lastSaved = System.currentTimeMillis();
		this.nodesVisited = s.getNodesVisited();
		this.areaType = s.getArea().getType();
		
		for (Entry<UUID, PlayerSessionData> ent : s.getParty().entrySet()) {
			partyIds.add(ent.getKey());
			party.put(ent.getValue().getData().getDisplay(), ent.getValue().getPlayerClass());
		}
	}
	
	public SessionSnapshot(ResultSet save, ResultSet party) throws SQLException {
		this.lastSaved = save.getLong("lastSaved");
		this.nodesVisited = save.getInt("nodesVisited");
		this.areaType = AreaType.valueOf(save.getString("areaType"));
		
		while (party.next()) {
			UUID uuid = UUID.fromString(party.getString("uuid"));
			String display = party.getString("display");
			PlayerClass pc = PlayerClass.valueOf(party.getString("playerClass"));
			partyIds.add(uuid);
			this.party.put(display, pc);
		}
	}
	
	public void displaySaveButton(CommandSender s, int saveSlot) {
		String text = SharedUtil.translateColors("&7&l[1] &7" + new Date(lastSaved));
		s.spigot().sendMessage(SharedUtil.createText(text, createHoverText(), "/nr save " + saveSlot).create());
	}
	
	public void displayLoadButton(CommandSender s, int saveSlot) {
		String text = SharedUtil.translateColors("&7&l[1] &7" + new Date(lastSaved));
		s.spigot().sendMessage(SharedUtil.createText(text, createHoverText(), "/nr load " + saveSlot).create());
	}
	
	private String createHoverText() {
		String text = "&7Area: &e" + areaType.getDisplay() + "\n&7Nodes visited: &e" + nodesVisited + "\n&7Party members:";
		for (Entry<String, PlayerClass> ent : party.entrySet()) {
			text += "\n&7- &e" + ent.getKey() + " &7[&c" + ent.getValue().getDisplay() + "]";
		}
		return SharedUtil.translateColors(text);
	}
}
