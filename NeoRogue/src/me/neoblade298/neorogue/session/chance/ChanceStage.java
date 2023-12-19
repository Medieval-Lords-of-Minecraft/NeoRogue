package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceStage {
	private static final TextComponent indivChoice = Component.text("Individual Choice - Each party member can choose their own fate", NamedTextColor.YELLOW),
			hostChoice = Component.text("Host Choice - Only the host may choose everyone's fate", NamedTextColor.YELLOW);
	
	protected ArrayList<ChanceChoice> choices = new ArrayList<ChanceChoice>();
	protected ArrayList<TextComponent> description = new ArrayList<TextComponent>();
	private String id;
	
	public ChanceStage(ChanceSet set, String id, String description) {
		this.id = id;
		this.description.add(0, set.isIndividual() ? indivChoice : hostChoice);
		for (TextComponent c : SharedUtil.addLineBreaks(Component.text(description, NamedTextColor.GRAY), 250)) {
			this.description.add((TextComponent) c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		set.addStage(this);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
	
	public String getId() {
		return id;
	}
}
