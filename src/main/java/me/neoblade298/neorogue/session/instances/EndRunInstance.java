package me.neoblade298.neorogue.session.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SessionStatsInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionType;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Shared base for the instances shown at the end of a run (win or lose). Handles the common
 * teleport, hologram/finance-block setup, scoreboard lines, cleanup, and interaction routing.
 * Subclasses supply the win/lose-specific end-of-run effects and result message.
 */
public abstract class EndRunInstance extends EditInventoryInstance {
	protected static final double SPAWN_X = Session.LOSE_X + 8.5, SPAWN_Z = Session.LOSE_Z + 7.5;
	protected TextDisplay holo, leaveHolo, financeHolo, expHolo;
	protected Location financeBlock, expBlock;

	public EndRunInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}

	/** Whether this run ended in a win (affects payout and finance summary framing). */
	protected abstract boolean isWin();

	/** Run-specific end-of-run effects (session triggers, payout, notoriety, etc.). */
	protected abstract void onRunEnd();

	/** The message broadcast to the party when the run ends. */
	protected abstract Component getResultMessage();

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
				Component.text("Right click to view stats!", NamedTextColor.WHITE));
		leaveHolo = NeoRogue.createHologram(spawn.clone().add(0, 2, -4),
				Component.text("Right click to leave!", NamedTextColor.WHITE));
		// Finances gold block, offset to the side of the stats/leave axis (adjust offset if it clips geometry)
		financeBlock = spawn.clone().add(2, -0.5, 0);
		financeHolo = NeoRogue.createHologram(spawn.clone().add(-3, 2, 0),
				Component.text("Right click to view finances!", NamedTextColor.GOLD));
		// Experience emerald block, mirrored across the stats/leave axis from the finances block
		expBlock = spawn.clone().add(-2, -0.5, 0);
		expBlock.getBlock().setType(Material.EMERALD_BLOCK);
		expHolo = NeoRogue.createHologram(spawn.clone().add(3, 2, 0),
				Component.text("Right click to view experience!", NamedTextColor.GREEN));

		s.broadcast(getResultMessage());
		PlayerManager.getPlayerData(s.getHost()).removeSnapshot(s.getSaveSlot());
		s.deleteSave();
	}

	// Records a finished-run result for each party member for winrate/winstreak stats. Tutorial and
	// endless runs are excluded so they don't distort the numbers.
	protected void recordRunResults() {
		if (s.getSessionType() == SessionType.TUTORIAL || s.isEndless()) return;
		boolean won = isWin();
		int notoriety = s.getNotoriety();
		int partySize = s.getParty().size();
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = PlayerManager.getPlayerData(psd.getUniqueId());
			if (pd != null) pd.addRunResult(psd.getPlayerClass(), notoriety, partySize, psd.getSessionStats().getExpEarned(), won);
		}
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost().equals(data.getUniqueId())) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		String line = "";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay();
		return line;
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		holo.remove();
		leaveHolo.remove();
		financeHolo.remove();
		if (expHolo != null) expHolo.remove();
		if (financeBlock != null) financeBlock.getBlock().setType(Material.AIR);
		if (expBlock != null) expBlock.getBlock().setType(Material.AIR);
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return null;
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
			Player p = e.getPlayer();
			if (e.getClickedBlock().getType() == Material.BEACON) {
				s.leavePlayer(p);
				return;
			}
			if (e.getClickedBlock().getType() == Material.LECTERN) {
				new SessionStatsInventory(p, s);
				return;
			}
			if (e.getClickedBlock().getType() == Material.GOLD_BLOCK) {
				RunReward.sendFinancesSummary(p, s, isWin());
				return;
			}
			if (e.getClickedBlock().getType() == Material.EMERALD_BLOCK) {
				RunReward.sendExpSummary(p, s);
				return;
			}
		}
		super.handleInteractEvent(e);
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
			Player p = e.getPlayer();
			Material type = e.getClickedBlock().getType();
			if (type == Material.BEACON) {
				e.setCancelled(true);
				s.removeSpectator(p);
				return;
			}
			if (type == Material.LECTERN) {
				e.setCancelled(true);
				new SessionStatsInventory(p, s);
				return;
			}
			if (type == Material.GOLD_BLOCK) {
				e.setCancelled(true);
				RunReward.sendFinancesSummary(p, s, isWin());
				return;
			}
			if (type == Material.EMERALD_BLOCK) {
				e.setCancelled(true);
				RunReward.sendExpSummary(p, s);
				return;
			}
		}
		super.handleSpectatorInteractEvent(e);
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {

	}
}
