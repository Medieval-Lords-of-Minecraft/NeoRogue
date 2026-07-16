package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SessionSnapshot {
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"MMM d yyyy h:mma");

	static {
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	}


	private long lastSaved;
	private int nodesVisited;
	private RegionType regionType;
	private int notoriety;
	private boolean endless;
	private SessionType sessionType = SessionType.STANDARD;
	private HashMap<String, EquipmentClass> party = new HashMap<String, EquipmentClass>();
	private HashMap<UUID, String> partyIds = new HashMap<UUID, String>();
	
	public SessionSnapshot(Session s) {
		this.lastSaved = System.currentTimeMillis();
		this.nodesVisited = s.getNodesVisited();
		this.regionType = s.getRegion().getType();
		this.notoriety = s.getNotoriety();
		this.endless = s.isEndless();
		this.sessionType = s.getSessionType();
		
		for (Entry<UUID, PlayerSessionData> ent : s.getParty().entrySet()) {
			partyIds.put(ent.getKey(), ent.getValue().getData().getDisplay());
			party.put(ent.getValue().getData().getDisplay(), ent.getValue().getPlayerClass());
		}
	}
	
	public SessionSnapshot(ResultSet save, ResultSet party) throws SQLException {
		this.lastSaved = save.getLong("lastSaved");
		this.nodesVisited = save.getInt("nodesVisited");
		this.regionType = RegionType.valueOf(save.getString("regionType"));
		this.notoriety = save.getInt("notoriety");
		this.endless = save.getInt("endless") == 1;
		this.sessionType = SessionType.fromStorage(save.getString("sessionType"));
		
		while (party.next()) {
			UUID uuid = UUID.fromString(party.getString("uuid"));
			String display = party.getString("display");
			EquipmentClass pc = EquipmentClass.valueOf(party.getString("playerClass"));
			partyIds.put(uuid, display);
			this.party.put(display, pc);
		}
	}
	
	public void displayNewButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[" + saveSlot + "] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text(new Date(lastSaved).toString(), NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.runCommand("/nr new " + saveSlot + " " + s.getName() + "Party"))
		.hoverEvent(HoverEvent.showText(createHoverText()));
		Util.msgRaw(s, text);
	}
	
	public static void displayEmptyNewButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[" + saveSlot + "] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text("Empty", NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.runCommand("/nr new " + saveSlot + " " + s.getName() + "Party"))
		.hoverEvent(HoverEvent.showText(Component.text("Click to start a new game on this slot!")));
		Util.msgRaw(s, text);
	}
	
	public void displayLoadButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[" + saveSlot + "] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text(sdf.format(new Date(lastSaved)), NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.runCommand("/nr load " + saveSlot))
		.hoverEvent(HoverEvent.showText(createHoverText()));
		Util.msgRaw(s, text);
	}
	
	private Component createHoverText() {
		Builder b = Component.text().content("Region: ").color(NamedTextColor.GRAY)
				.append(Component.text(regionType.getDisplay(), NamedTextColor.GOLD))
				.append(Component.text("\nNodes visited: ", NamedTextColor.GRAY))
				.append(Component.text(nodesVisited, NamedTextColor.GOLD))
				.append(Component.text("\nParty members:", NamedTextColor.GRAY));
		for (Entry<String, EquipmentClass> ent : party.entrySet()) {
			b.append(Component.text("\n- ", NamedTextColor.GRAY))
			.append(Component.text(ent.getKey(), NamedTextColor.YELLOW))
			.append(Component.text(" [", NamedTextColor.GRAY))
			.append(Component.text(ent.getValue().getDisplay(), NamedTextColor.RED))
			.append(Component.text("]", NamedTextColor.GRAY));
		}
		return b.build();
	}
	
	public HashMap<UUID, String> getPartyIds() {
		return partyIds;
	}

	public long getLastSaved() {
		return lastSaved;
	}

	public RegionType getRegionType() {
		return regionType;
	}

	public int getNodesVisited() {
		return nodesVisited;
	}

	public HashMap<String, EquipmentClass> getParty() {
		return party;
	}

	public int getNotoriety() {
		return notoriety;
	}

	public boolean isEndless() {
		return endless;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	// Party members of the saved run mapped to the class each was playing.
	public HashMap<UUID, EquipmentClass> getPartyClasses() {
		HashMap<UUID, EquipmentClass> out = new HashMap<UUID, EquipmentClass>();
		for (Entry<UUID, String> ent : partyIds.entrySet()) {
			EquipmentClass ec = party.get(ent.getValue());
			if (ec != null) out.put(ent.getKey(), ec);
		}
		return out;
	}
}
