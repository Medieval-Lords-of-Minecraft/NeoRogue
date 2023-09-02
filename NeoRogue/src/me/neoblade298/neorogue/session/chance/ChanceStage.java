package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public class ChanceStage {
	private static HashMap<String, ChanceStage> stages = new HashMap<String, ChanceStage>();
	
	protected ArrayList<ChanceChoice> choices = new ArrayList<ChanceChoice>();
	protected ArrayList<String> description;
	private String id;
	
	public static ChanceStage get(String id) {
		return stages.get(id);
	}
	
	public ChanceStage(String id, String description) {
		this.id = id;
		this.description = SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY);
		stages.put(id, this);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
	
	public String getId() {
		return id;
	}
}
