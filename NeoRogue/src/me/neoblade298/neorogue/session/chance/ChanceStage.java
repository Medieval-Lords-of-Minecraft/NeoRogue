package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public class ChanceStage {
	protected ArrayList<ChanceChoice> choices = new ArrayList<ChanceChoice>();
	protected ArrayList<String> description;
	private String id;
	
	public ChanceStage(String id, String description) {
		this.id = id;
		this.description = SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
}
