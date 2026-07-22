package me.neoblade298.neorogue.tutorial.book;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import net.kyori.adventure.inventory.Book;

// Loads the configurable book-UI tutorials from tutorials.yml (top-level key = book id) and handles
// opening their table of contents / chapters, plus granting the one-time per-chapter read rewards.
public class TutorialBookRegistry {
	// The player command used by the in-book navigation links (see BookCommand). Kept here so the
	// generated ClickEvents and the command registration stay in sync.
	public static final String COMMAND = "nrbook";

	private static final LinkedHashMap<String, TutorialBook> books = new LinkedHashMap<String, TutorialBook>();

	private TutorialBookRegistry() {
	}

	public static synchronized void reload() {
		books.clear();
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "tutorials.yml"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					books.put(key.toLowerCase(), new TutorialBook(key.toLowerCase(), yml.getSection(key)));
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load tutorial book " + key
							+ " in file " + file.getName());
				}
			}
		});
	}

	public static TutorialBook get(String id) {
		return id == null ? null : books.get(id.toLowerCase());
	}

	public static Collection<TutorialBook> getBooks() {
		return books.values();
	}

	public static List<String> getBookIds() {
		return new ArrayList<String>(books.keySet());
	}

	// Every book's chapter read-flags, for FlagRegistry listing/tab-completion.
	public static List<String> getReadFlags() {
		List<String> flags = new ArrayList<String>();
		for (TutorialBook book : books.values()) flags.addAll(book.getReadFlags());
		return flags;
	}

	// Opens the table of contents for a book.
	public static boolean openTableOfContents(Player p, String bookId) {
		TutorialBook book = get(bookId);
		if (book == null) {
			Util.displayError(p, "That tutorial doesn't exist!");
			return false;
		}
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		p.openBook(book.buildTableOfContents(pd));
		return true;
	}

	// Opens a chapter, granting the one-time read flag + book-level reward commands the first time
	// this player reaches it.
	public static boolean openChapter(Player p, String bookId, int index) {
		TutorialBook book = get(bookId);
		if (book == null) {
			Util.displayError(p, "That tutorial doesn't exist!");
			return false;
		}
		TutorialBook.Chapter chapter = book.getChapter(index);
		if (chapter == null) {
			Util.displayError(p, "That chapter doesn't exist!");
			return false;
		}

		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd != null) {
			String flag = book.getReadFlag(chapter);
			if (!pd.hasFlag(flag)) {
				pd.addFlag(flag);
				runRewards(p, book.getRewardCommands());
			}
		}

		Book built = book.buildChapter(index);
		if (built == null) return false;
		p.openBook(built);
		return true;
	}

	private static void runRewards(Player p, List<String> commands) {
		for (String cmd : commands) {
			String parsed = cmd.replace("%player%", p.getName()).replace("%uuid%", p.getUniqueId().toString());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
		}
	}
}
