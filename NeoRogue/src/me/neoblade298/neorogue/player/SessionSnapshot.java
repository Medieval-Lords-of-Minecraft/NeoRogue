package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SessionSnapshot {
	private long lastSaved;
	private int nodesVisited;
	private AreaType areaType;
	private HashMap<String, EquipmentClass> party = new HashMap<String, EquipmentClass>();
	private HashMap<UUID, String> partyIds = new HashMap<UUID, String>();
	
	public SessionSnapshot(Session s) {
		this.lastSaved = System.currentTimeMillis();
		this.nodesVisited = s.getNodesVisited();
		this.areaType = s.getArea().getType();
		
		for (Entry<UUID, PlayerSessionData> ent : s.getParty().entrySet()) {
			partyIds.put(ent.getKey(), ent.getValue().getData().getDisplay());
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
			EquipmentClass pc = EquipmentClass.valueOf(party.getString("playerClass"));
			partyIds.put(uuid, display);
			this.party.put(display, pc);
		}
	}
	
	public void displayNewButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[1] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text(new Date(lastSaved).toString(), NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.suggestCommand("/nr new " + saveSlot + " partyname"))
		.hoverEvent(HoverEvent.showText(createHoverText()));
		Util.msg(s, text);
	}
	
	public static void displayEmptyNewButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[1] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text("Empty", NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.suggestCommand("/nr new " + saveSlot + " partyname"))
		.hoverEvent(HoverEvent.showText(Component.text("Click to start a new game on this slot!")));
		Util.msg(s, text);
	}
	
	public void displayLoadButton(CommandSender s, int saveSlot) {
		Component text = Component.text().content("[1] ").color(NamedTextColor.GRAY)
				.decorate(TextDecoration.BOLD)
				.append(Component.text(new Date(lastSaved).toString(), NamedTextColor.GRAY)).build();
		text = text.clickEvent(ClickEvent.suggestCommand("/nr load " + saveSlot))
		.hoverEvent(HoverEvent.showText(createHoverText()));
		Util.msg(s, text);
	}
	
	private Component createHoverText() {
		Builder b = Component.text().content("Area: ").color(NamedTextColor.GRAY)
				.append(Component.text(areaType.getDisplay(), NamedTextColor.GOLD))
				.append(Component.text("\nNodes visited: ", NamedTextColor.GRAY))
				.append(Component.text(nodesVisited, NamedTextColor.GOLD))
				.append(Component.text("\nParty members:", NamedTextColor.GRAY));
		for (Entry<String, EquipmentClass> ent : party.entrySet()) {
			b.append(Component.text("\n- ", NamedTextColor.GRAY))
			.append(Component.text(ent.getKey(), NamedTextColor.YELLOW))
			.append(Component.text("[", NamedTextColor.GRAY))
			.append(Component.text(ent.getValue().getDisplay(), NamedTextColor.RED))
			.append(Component.text("]", NamedTextColor.GRAY));
		}
		return b.build();
	}
	
	public HashMap<UUID, String> getPartyIds() {
		return partyIds;
	}
}
