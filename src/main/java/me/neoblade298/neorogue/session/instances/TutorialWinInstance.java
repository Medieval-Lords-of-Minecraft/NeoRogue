package me.neoblade298.neorogue.session.instances;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Win screen shown at the end of the (shortened) tutorial run. Unlike the standard {@link WinInstance},
 * a tutorial has no coins, notoriety, or caravan rewards to hand out, so the normal win payout is
 * skipped entirely. The shared {@link EndRunInstance} teleport/stats/leave handling still applies.
 */
public class TutorialWinInstance extends WinInstance {
	public TutorialWinInstance(Session s) {
		super(s);
	}

	// Tutorials grant no run rewards (payout, notoriety, region-completion), so skip WinInstance's payout.
	@Override
	protected void onRunEnd() {
	}

	@Override
	protected Component getResultMessage() {
		return Component.text("Tutorial complete! You're ready for the real thing.", NamedTextColor.GREEN);
	}

	@Override
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();

		onRunEnd();
		recordRunResults();

		holo = NeoRogue.createHologram(spawn.clone().add(0, 2, 4),
				Component.text("Congratulations! You're done!", NamedTextColor.GREEN).appendNewline()
						.append(Component.text("In-depth tutorials are available")).appendNewline()
						.append(Component.text("in your compass.")).appendNewline()
						.append(Component.text("Leave with the beacon!")));
		leaveHolo = NeoRogue.createHologram(spawn.clone().add(0, 2, -4),
				Component.text("Right click to leave!", NamedTextColor.WHITE));

		s.broadcast(getResultMessage());
		PlayerManager.getPlayerData(s.getHost()).removeSnapshot(s.getSaveSlot());
		s.deleteSave();
	}
}
