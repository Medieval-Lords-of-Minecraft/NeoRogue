package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public class ChanceStage {
	protected ArrayList<ChanceChoice> choices;
	protected ArrayList<String> description;
	
	public ChanceStage(String description) {
		this.description = SharedUtil.addLineBreaks(description, 250, ChatColor.GRAY);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
}
