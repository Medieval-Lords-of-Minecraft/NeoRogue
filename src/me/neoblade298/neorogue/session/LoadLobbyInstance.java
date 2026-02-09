package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class LoadLobbyInstance extends LobbyInstance {
    private static final double SPAWN_X = Session.LOAD_LOBBY_X + 6.5, SPAWN_Z = Session.LOAD_LOBBY_Z + 3.5, HOLO_X = 0,
            HOLO_Y = 3, HOLO_Z = 10;

    private Instance startInstance;

    public LoadLobbyInstance(Player host, Session s) {
        super(host, s, SPAWN_X, SPAWN_Z);
		partyInfoHeader = Component.text().content("<< ( ").color(NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.RED)).append(Component.text(" ) >>"))
				.append(Component.text("\nPlayers:")).build();

		// Setup hologram
		Component text = Component.text("Wait for players to join,").appendNewline()
			.append(Component.text("then click the button!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
        
        playerLines.add("§7...");
    }

    // Used by Session to set the instance to start after loading
    public void completeLoad(Instance startInstance) {
        this.startInstance = startInstance;
        updateBoardLines();

        // Invite all members of the session
        for (PlayerSessionData data : s.getParty().values()) {
            if (data.getUniqueId().equals(host))
                continue;
            invited.add(data.getUniqueId());
            Util.msg(data.getPlayer(), Component.text("You've been invited to ")
                    .append(Component.text(name, NamedTextColor.YELLOW)).append(Component.text("!")));
            Util.msg(data.getPlayer(), NeoCore.miniMessage().deserialize(invPrefix + Bukkit.getPlayer(host).getName() + invSuffix));
        }
    }
    

	@Override
	public void updateBoardLines() {
		playerLines.clear();

        // Host at top
        PlayerSessionData hostData = session.getParty().get(host);
        playerLines.add(createBoardLine(hostData, true));

        for (PlayerSessionData data : session.getParty().values()) {
            if (data.getUniqueId().equals(host)) continue;
            String line = createBoardLine(data, false);
            playerLines.add(line);
        }
	}

    private String createBoardLine(PlayerSessionData data, boolean isHost) {
        UUID uuid = data.getUniqueId();
        String line = inLobby.contains(uuid) ? "§a✓ §f" : "§c✗ §f";
        if (isHost) {
            line += "★ ";
        }
        line += data.getData().getDisplay() + "§7 - §e" + data.getPlayerClass().getDisplay();
        return line;
    }

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
        e.setCancelled(true);
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
        UUID uuid = e.getPlayer().getUniqueId();
		
		if (e.getClickedBlock().getType() == Material.STONE_BUTTON) {
            if (!s.getHost().equals(uuid)) {
                Util.displayError(e.getPlayer(), "Only the host may start the game!");
                return;
            }
            startGame();
			return;
		}
    }


    @Override
    public void addPlayer(Player p) {
		if (s.isBusy()) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

        inLobby.add(p.getUniqueId());
		p.setGameMode(GameMode.SURVIVAL);
		SessionManager.addToSession(p.getUniqueId(), this.s);
		p.teleport(spawn);
		displayInfo(p);
		TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" joined the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
		updateBoardLines();
    }


    @Override
    public void kickPlayer(Player s, String name) {
        if (!s.getUniqueId().equals(host)) {
            Util.msgRaw(s, hostOnlyKick);
            return;
        }

        if (this.s.isBusy()) {
            Util.msgRaw(s, gameGenerating);
            return;
        }

        Player p = Bukkit.getPlayer(name);

        if (!inLobby.contains(p.getUniqueId())) {
            Util.msgRaw(s, playerNotInLobby);
            return;
        }

        SessionManager.removeFromSession(p.getUniqueId());
        inLobby.remove(p.getUniqueId());
        p.teleport(NeoRogue.spawn);
        TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
                .append(Component.text(" was kicked from the lobby!", NamedTextColor.GRAY)).build();
        broadcast(tc);
        updateBoardLines();
    }


    @Override
    public void startGame() {
        // First remove players that weren't in lobby
        ArrayList<UUID> toRemove = new ArrayList<UUID>();
        for (UUID uuid : session.getParty().keySet()) {
            if (!inLobby.contains(uuid)) {
                toRemove.add(uuid);
            }
        }
        for (UUID uuid : toRemove) {
            session.getParty().remove(uuid);
        }

		s.getRegion().instantiate();
		s.setInstance(startInstance);
		s.updateSpectatorLines();
		Util.msgRaw(Bukkit.getPlayer(host), Component.text("Finished loading.", NamedTextColor.GRAY));

		for (PlayerSessionData psd : s.getParty().values()) {
			psd.setupInventory();
			psd.syncHealth();
		}
    }

    // Displays anytime a player joins and on command
    @Override
    public void displayInfo(Player viewer) {
        Player h = Bukkit.getPlayer(host);
        boolean isHost = viewer.getUniqueId().equals(host);

        // Replace to add party name to header
        boolean first = true;
        Util.msgRaw(viewer, partyInfoHeader.replaceText(config -> {
            config.match("%");
            config.replacement(Component.text(name, NamedTextColor.RED));
        }));

        TextComponent hostText = Component.text().content("- ").color(NamedTextColor.GRAY)
                .append(Component.text(h.getName(), NamedTextColor.GOLD))
                .append(Component.text(" (")
                        .append(Component.text(h.getName(), NamedTextColor.YELLOW)))
                .append(Component.text(") (")).append(Component.text("Host", NamedTextColor.YELLOW))
                .append(Component.text(")")).build();
        Util.msgRaw(viewer, hostText);
        String str = "";
        if (inLobby.size() > 1) {
            first = true;
            for (UUID uuid : inLobby) {
                if (uuid.equals(host))
                    continue;
                if (!first) {
                    str += "\n";
                }
                first = false;
                Player p = Bukkit.getPlayer(uuid);
                str += "<gray>- <gold>" + p.getName() + "</gold> (<yellow>" + p.getName()
                        + "</yellow>)</gray>";
                if (isHost) {
                    str += " <dark_gray>[<red><click:run_command:'/nr kick " + p.getName()
                            + "'><hover:show_text:'Click to kick " + p.getName()
                            + "'>Click to kick</hover></click></red>]";
                }
            }
            Util.msgRaw(viewer, NeoCore.miniMessage().deserialize(str));
        }
    }
}
