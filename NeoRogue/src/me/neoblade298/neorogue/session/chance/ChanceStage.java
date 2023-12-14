package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import me.neoblade298.neocore.shared.util.SharedUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceStage {
	private static HashMap<String, ChanceStage> stages = new HashMap<String, ChanceStage>();
	
	protected ArrayList<ChanceChoice> choices = new ArrayList<ChanceChoice>();
	protected ArrayList<TextComponent> description = new ArrayList<TextComponent>();
	private String id;
	
	public static ChanceStage get(String id) {
		return stages.get(id);
	}
	
	public ChanceStage(String id, String description) {
		this.id = id;
		for (TextComponent c : SharedUtil.addLineBreaks(Component.text(description, NamedTextColor.GRAY), 250)) {
			this.description.add((TextComponent) c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		stages.put(id, this);
	}
	
	public void addChoice(ChanceChoice choice) {
		this.choices.add(choice);
	}
	
	public String getId() {
		return id;
	}
}
