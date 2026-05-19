package me.neoblade298.neorogue.session.chance;

import java.util.List;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.TextComponent;

public interface ChanceDescriptionSupplier {
	public List<TextComponent> get(Session s, ChanceInstance inst, PlayerSessionData data);
}
