package me.neoblade298.neorogue.tutorial.book;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.neoblade298.neocore.bukkit.util.Util;

// Brigadier command backing the book-UI tutorials and their in-book navigation links:
//   /nrbook <book>            -> open the table of contents
//   /nrbook <book> <chapter>  -> open a chapter (and grant its one-time read reward)
@SuppressWarnings("UnstableApiUsage")
public class BookCommand {
	private BookCommand() {
	}

	public static LiteralCommandNode<CommandSourceStack> build() {
		return Commands.literal("nrbook")
				.requires(src -> src.getSender() instanceof Player)
				.then(Commands.argument("book", StringArgumentType.word())
						.suggests(BookCommand::suggestBooks)
						.executes(BookCommand::openTableOfContents)
						.then(Commands.argument("chapter", IntegerArgumentType.integer(0))
								.executes(BookCommand::openChapter)))
				.executes(ctx -> {
					Util.displayError((Player) ctx.getSource().getSender(), "Usage: /nrbook <book> [chapter]");
					return Command.SINGLE_SUCCESS;
				})
				.build();
	}

	private static int openTableOfContents(CommandContext<CommandSourceStack> ctx) {
		Player p = (Player) ctx.getSource().getSender();
		TutorialBookRegistry.openTableOfContents(p, StringArgumentType.getString(ctx, "book"));
		return Command.SINGLE_SUCCESS;
	}

	private static int openChapter(CommandContext<CommandSourceStack> ctx) {
		Player p = (Player) ctx.getSource().getSender();
		TutorialBookRegistry.openChapter(p, StringArgumentType.getString(ctx, "book"),
				IntegerArgumentType.getInteger(ctx, "chapter"));
		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Suggestions> suggestBooks(CommandContext<CommandSourceStack> ctx,
			SuggestionsBuilder builder) {
		String remaining = builder.getRemainingLowerCase();
		for (String id : TutorialBookRegistry.getBookIds()) {
			if (id.toLowerCase().startsWith(remaining)) builder.suggest(id);
		}
		return builder.buildFuture();
	}
}
