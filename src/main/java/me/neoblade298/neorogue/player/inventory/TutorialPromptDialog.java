package me.neoblade298.neorogue.player.inventory;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;

public final class TutorialPromptDialog {
	private TutorialPromptDialog() {
	}

	public static void show(Player player) {
		UUID playerId = player.getUniqueId();
		ActionButton start = ActionButton.builder(Component.text("Start Tutorial", NamedTextColor.GREEN))
				.width(200)
				.action(DialogAction.customClick((response, audience) -> {
					Player currentPlayer = Bukkit.getPlayer(playerId);
					if (currentPlayer == null) return;
					currentPlayer.closeDialog();
					if (!SessionManager.createTutorialSession(currentPlayer, 1)) return;
					PlayerData data = PlayerManager.getPlayerData(playerId);
					if (data != null) data.addFlag(PlayerData.FLAG_PLAYED_BEFORE);
				}, ClickCallback.Options.builder().uses(1).build()))
				.build();

		Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(Component.text("Tutorial", NamedTextColor.GOLD))
						.canCloseWithEscape(true)
						.body(List.of(DialogBody.plainMessage(Component.text(
								"You look strong. If you help me escort my caravan to trade, I'll give you some starting money."))))
						.build())
				.type(DialogType.multiAction(List.of(start)).columns(1).build()));
		player.showDialog(dialog);
	}
}
