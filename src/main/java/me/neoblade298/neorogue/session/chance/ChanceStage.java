package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceStage {
	protected ArrayList<ChanceChoice> choices = new ArrayList<ChanceChoice>();
	protected TextComponent description;
	private String id;
	
	public ChanceStage(ChanceSet set, String id, String description) {
		this.id = id;
		this.description = Component.text(description, NamedTextColor.WHITE)
				.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
		set.addStage(this);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
	
	public String getId() {
		return id;
	}
}
