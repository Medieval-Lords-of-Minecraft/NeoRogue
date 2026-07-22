package me.neoblade298.neorogue.tutorial.book;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.neoblade298.neorogue.NeoRogue;
import net.kyori.adventure.key.Key;

// Handles the in-book navigation for book-UI tutorials. The book links use the minecraft:custom click
// action (Adventure ClickEvent.custom) instead of run_command, so the vanilla client never shows the
// "Confirm Command Execution" popup. The custom event carries a key like "neorogue:book/<book>" (open
// the table of contents) or "neorogue:book/<book>/<chapter>" (open a chapter + grant its read reward).
@SuppressWarnings("UnstableApiUsage")
public class BookClickListener implements Listener {
	@EventHandler
	public void onCustomClick(PlayerCustomClickEvent e) {
		Key id = e.getIdentifier();
		if (!id.namespace().equals("neorogue")) return;
		String value = id.value();
		if (!value.startsWith("book/")) return;
		if (!(e.getCommonConnection() instanceof PlayerGameConnection game)) return;

		Player p = game.getPlayer();
		String[] parts = value.split("/");
		// parts[0] = "book", parts[1] = book id, parts[2] = optional chapter index
		if (parts.length < 2) return;
		String bookId = parts[1];
		int chapter = -1;
		if (parts.length >= 3) {
			try {
				chapter = Integer.parseInt(parts[2]);
			} catch (NumberFormatException ex) {
				return;
			}
		}

		// openBook / reward dispatch must run on the main thread.
		final int index = chapter;
		Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> {
			if (index < 0) TutorialBookRegistry.openTableOfContents(p, bookId);
			else TutorialBookRegistry.openChapter(p, bookId, index);
		});
	}
}
