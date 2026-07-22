package me.neoblade298.neorogue.tutorial.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.player.FlagRegistry;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

// A single configurable book-UI tutorial loaded from tutorials.yml. Holds the book's display title,
// its shared (book-level) reward commands, and an ordered list of chapters.
//
// The book is presented as multiple Adventure books rather than one:
//   - A "table of contents" book whose pages list every chapter as a clickable link.
//   - One "chapter" book per chapter (opened when its link is clicked), whose page 1 is the chapter
//     start so the vanilla client always opens on it.
// Navigation uses ClickEvent.runCommand (not changePage) because the server never sees client-side
// page turns, and we must observe a player reaching a chapter to grant its one-time reward. Because
// the read flag is one-time per chapter, invoking the command directly is equivalent to reading it,
// so there is no reward exploit.
public class TutorialBook {
	// Visual width (in stripped characters) a content line is wrapped to, and the number of lines a
	// single book page can show. Conservative defaults that fit the vanilla book font; overridable
	// per book via "line-width" / "lines-per-page".
	private static final int DEFAULT_LINE_WIDTH = 19;
	private static final int DEFAULT_LINES_PER_PAGE = 14;

	// custom click events must carry a non-null payload or the book's written_book_content fails to
	// serialize (all navigation data is encoded in the click key, so an empty compound suffices).
	private static final BinaryTagHolder EMPTY_PAYLOAD = BinaryTagHolder.binaryTagHolder("{}");

	private final String id;
	private final String rawTitle;
	private final List<String> rewardCommands;
	private final List<Chapter> chapters = new ArrayList<Chapter>();
	private final int lineWidth;
	private final int linesPerPage;

	public TutorialBook(String id, Section sec) {
		this.id = id;
		this.rawTitle = sec.getString("title", id);
		this.lineWidth = Math.max(4, sec.getInt("line-width", DEFAULT_LINE_WIDTH));
		this.linesPerPage = Math.max(4, sec.getInt("lines-per-page", DEFAULT_LINES_PER_PAGE));

		List<String> rewards = sec.getStringList("rewards");
		this.rewardCommands = rewards != null ? rewards : new ArrayList<String>();

		Section chapterSec = sec.getSection("chapters");
		if (chapterSec != null) {
			for (String key : chapterSec.getKeys()) {
				Section cs = chapterSec.getSection(key);
				if (cs == null) continue;
				chapters.add(new Chapter(key, cs));
			}
			// getKeys() isn't guaranteed to preserve YAML order, so order the table of contents by
			// each chapter's "priority" (ascending). A stable sort keeps ties in their loaded order.
			chapters.sort((a, b) -> Integer.compare(a.priority, b.priority));
		}
	}

	public String getId() {
		return id;
	}

	public List<String> getRewardCommands() {
		return Collections.unmodifiableList(rewardCommands);
	}

	public int getChapterCount() {
		return chapters.size();
	}

	public Chapter getChapter(int index) {
		return index >= 0 && index < chapters.size() ? chapters.get(index) : null;
	}

	// The one-time "read" flag for a chapter, e.g. "tutorial:book:welcome:combat".
	public String getReadFlag(Chapter chapter) {
		return FlagRegistry.TUTORIAL + ":book:" + id + ":" + chapter.id;
	}

	// Every chapter read-flag this book can grant, for FlagRegistry listing/tab-completion.
	public List<String> getReadFlags() {
		List<String> flags = new ArrayList<String>();
		for (Chapter c : chapters) flags.add(getReadFlag(c));
		return flags;
	}

	// Builds the clickable table-of-contents book. Each entry runs the chapter command, and read
	// chapters are marked with a check. pd may be null (read state simply won't be shown).
	public Book buildTableOfContents(PlayerData pd) {
		List<Component> pages = new ArrayList<Component>();
		TextComponent.Builder page = Component.text();
		int used = 0;

		// Header (title + "Table of Contents" + divider) only on the first page.
		page.append(mm(rawTitle)).append(Component.newline());
		page.append(Component.text("Table of Contents", NamedTextColor.DARK_GRAY)).append(Component.newline());
		page.append(divider()).append(Component.newline());
		used += 3;

		for (int i = 0; i < chapters.size(); i++) {
			if (used >= linesPerPage) {
				pages.add(page.build());
				page = Component.text();
				used = 0;
			}
			Chapter chapter = chapters.get(i);
			boolean read = pd != null && pd.hasFlag(getReadFlag(chapter));
			page.append(tocEntry(chapter, i, read)).append(Component.newline());
			used++;
		}
		pages.add(page.build());
		return Book.book(mm(rawTitle), Component.empty(), pages);
	}

	// Builds the book for a single chapter: page 1 is the chapter start (so openBook lands on it),
	// content flows across as many pages as needed, and every page has a "back to contents" footer.
	public Book buildChapter(int index) {
		Chapter chapter = getChapter(index);
		if (chapter == null) return null;

		Component back = mm("<dark_gray>\u00ab </dark_gray><gold><u>Back to Contents</u></gold>")
				.clickEvent(ClickEvent.custom(Key.key("neorogue", "book/" + id), EMPTY_PAYLOAD))
				.hoverEvent(HoverEvent.showText(Component.text("Return to the table of contents")));

		// Header on page 1: chapter name + divider. The footer (blank + back link) sits on every page.
		List<String> headerLines = wrap(chapter.rawName);
		int headerHeight = headerLines.size() + 1; // name lines + divider
		int footerHeight = 2; // blank line + back link

		List<String> content = new ArrayList<String>();
		for (String line : chapter.contentLines) {
			content.addAll(wrap(line));
		}

		List<Component> pages = new ArrayList<Component>();
		int idx = 0;
		boolean first = true;
		while (idx < content.size() || first) {
			int capacity = linesPerPage - footerHeight - (first ? headerHeight : 0);
			if (capacity < 1) capacity = 1;
			int end = Math.min(content.size(), idx + capacity);
			List<String> chunk = content.subList(idx, end);

			TextComponent.Builder page = Component.text();
			if (first) {
				page.append(mm(chapter.rawName)).append(Component.newline());
				page.append(divider()).append(Component.newline());
			}
			if (!chunk.isEmpty()) {
				page.append(mm(String.join("\n", chunk)));
			}
			page.append(Component.newline()).append(Component.newline()).append(back);
			pages.add(page.build());

			idx = end;
			first = false;
			if (idx >= content.size()) break;
		}
		return Book.book(mm(rawTitle), Component.empty(), pages);
	}

	private Component tocEntry(Chapter chapter, int index, boolean read) {
		Component marker = read
				? Component.text("\u2714 ", NamedTextColor.GREEN)
				: Component.text("\u00bb ", NamedTextColor.DARK_GRAY);
		String hover = read ? "Already read - click to revisit" : "Click to read this chapter";
		return Component.text()
				.append(marker)
				.append(mm(chapter.rawName))
				.build()
				.clickEvent(ClickEvent.custom(Key.key("neorogue", "book/" + id + "/" + index), EMPTY_PAYLOAD))
				.hoverEvent(HoverEvent.showText(Component.text(hover)));
	}

	private static Component divider() {
		return Component.text("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501", NamedTextColor.DARK_GRAY);
	}

	private static Component mm(String s) {
		return NeoCore.miniMessage().deserialize(s);
	}

	// Greedy word-wrap that measures width with MiniMessage tags stripped, so formatting doesn't count
	// against the line length. Formatting still applies because a page's lines are deserialized together.
	private List<String> wrap(String line) {
		List<String> out = new ArrayList<String>();
		if (line == null || line.isEmpty()) {
			out.add("");
			return out;
		}
		String[] words = line.split(" ");
		StringBuilder current = new StringBuilder();
		int currentLen = 0;
		for (String word : words) {
			int wordLen = visibleLength(word);
			if (currentLen > 0 && currentLen + 1 + wordLen > lineWidth) {
				out.add(current.toString());
				current.setLength(0);
				currentLen = 0;
			}
			if (currentLen > 0) {
				current.append(' ');
				currentLen++;
			}
			current.append(word);
			currentLen += wordLen;
		}
		out.add(current.toString());
		return out;
	}

	private static int visibleLength(String s) {
		return s.replaceAll("<[^>]*>", "").length();
	}

	// A single chapter: its id (used in the read flag), its MiniMessage display name, and its raw
	// content lines (split on newlines; wrapping/pagination happens when the book is built).
	public static class Chapter {
		private final String id;
		private final String rawName;
		private final int priority;
		private final List<String> contentLines;

		private Chapter(String id, Section sec) {
			this.id = id;
			this.rawName = sec.getString("name", id);
			this.priority = sec.getInt("priority", 0);
			// content may be a "|" block scalar (String) or a list of lines. NeoCore's getString/
			// getStringList strictly cast, so probe both defensively and join a list with newlines.
			String content = "";
			try {
				String raw = sec.getString("content", null);
				if (raw != null) content = raw;
			} catch (ClassCastException ignored) {
				// content is a list, not a scalar; fall through to getStringList below
			}
			if (content.isEmpty()) {
				try {
					List<String> list = sec.getStringList("content");
					if (list != null && !list.isEmpty()) content = String.join("\n", list);
				} catch (ClassCastException ignored) {
					// content is a scalar we already read (or absent)
				}
			}
			this.contentLines = new ArrayList<String>();
			for (String l : content.split("\n", -1)) {
				this.contentLines.add(l);
			}
		}

		public String getId() {
			return id;
		}
	}
}
