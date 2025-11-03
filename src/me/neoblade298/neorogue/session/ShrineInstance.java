package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShrineInstance extends EditInventoryInstance {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).count(50).spread(2, 2).speed(0.1);
	private static final double SPAWN_X = Session.SHRINE_X + 5.5, SPAWN_Z = Session.SHRINE_Z + 2.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 7;
	private static final int INIT_STATE = 0, REST_STATE = 1, UPGRADE_STATE = 2, RETURN_STATE = 3, RETURN_FAIL_STATE = 4;
	private int state = 0;
	private Block blockBottom, blockMiddle, blockTop;
	private HashSet<UUID> notUsed = new HashSet<UUID>();
	private TextDisplay holo;
	
	public ShrineInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
		spectatorLines = playerLines;
	}
	
	public ShrineInstance(Session s, String data, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		state = Integer.parseInt(data.substring(data.length() - 1));
		
		for (PlayerSessionData pd : party.values()) {
			if (pd.getInstanceData().equals("F")) {
				notUsed.add(pd.getPlayer().getUniqueId());
			}
		}

	}

	@Override
	public void setup() {
		blockBottom = spawn.clone().add(0, 0, 7).getBlock();
		blockMiddle = blockBottom.getRelative(BlockFace.UP);
		blockTop = blockMiddle.getRelative(BlockFace.UP);
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			notUsed.add(p.getUniqueId());
			teleportRandomly(p);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();

		// Setup hologram
		Component text = Component.text("Right click the").appendNewline().append(Component.text("emerald blocks", NamedTextColor.GREEN)).append(Component.text("!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost() == data.getUniqueId()) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		UUID uuid = data.getUniqueId();
		String line = !notUsed.contains(uuid) ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "(Host) ";
		}
		line += data.getData().getDisplay() + " (" + Math.round(data.getHealth()) + " / " + (int) data.getMaxHealth()
				+ "§c♥§f)";
		return line;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		blockBottom.setType(Material.EMERALD_BLOCK);
		blockMiddle.setType(Material.EMERALD_BLOCK);
		blockTop.setType(Material.AIR);
		holo.remove();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);

		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			if (state == RETURN_FAIL_STATE) {
				returnToNodes();
				return;
			}
			
			if (e.getClickedBlock().getType() == Material.EMERALD_BLOCK && state == INIT_STATE) {
				new ShrineChoiceInventory(p, s.getParty().get(p.getUniqueId()), this, s.getHost().equals(uuid));
				return;
			}

			if (e.getClickedBlock().getType() == Material.ANVIL && notUsed.contains(uuid) && state == UPGRADE_STATE) {
				new ShrineUpgradeInventory(p, s.getData(p.getUniqueId()), this);
			}
			else {
				super.handleInteractEvent(e);
			}
		}
		else {
			super.handleInteractEvent(e);
		}
	}
	
	// True if close inventory after suggesting
	public boolean suggestState(Player p, boolean rest) {
		if (!s.canSuggest()) return false;
		String suggestion = rest ? "resting" : "upgrading";
		s.setSuggestCooldown();
		s.broadcast(
			p.name().color(NamedTextColor.YELLOW)
			.append(Component.text(" suggests ", NamedTextColor.GRAY))
			.append(Component.text(suggestion, NamedTextColor.YELLOW))
			.append(Component.text("!", NamedTextColor.GRAY))
		);
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		return true;
	}

	public void chooseState(boolean rest) {
		state = rest ? REST_STATE : UPGRADE_STATE;
		s.broadcast("The host has chosen to <yellow>" + (rest ? "rest" : "upgrade"));
		part.play(blockMiddle.getLocation());
		s.broadcastSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		holo.remove();
		if (rest) {
			blockBottom.setType(Material.LODESTONE);
			blockMiddle.setType(Material.DIORITE_WALL);
			Wall wall = (Wall) blockMiddle.getBlockData();
			wall.setHeight(BlockFace.EAST, Height.LOW);
			wall.setHeight(BlockFace.WEST, Height.LOW);
			blockMiddle.setBlockData(wall);
			blockTop.setType(Material.SKELETON_SKULL);
			notUsed.clear();
			
			for (PlayerSessionData data : s.getParty().values()) {
				data.healPercent(0.35);
			}
			// Has to be done after everyone is healed
			for (PlayerSessionData data : s.getParty().values()) {
				data.updateBoardLines();
			}
			
			returnToNodes();
		}
		else {
			blockBottom.setType(Material.REINFORCED_DEEPSLATE);
			blockMiddle.setType(Material.ANVIL);
			Directional anvil = (Directional) blockMiddle.getBlockData();
			anvil.setFacing(BlockFace.EAST);
			blockMiddle.setBlockData(anvil);
			blockTop.setType(Material.WITHER_SKELETON_SKULL);

			Component text = Component.text("Use the anvil!").appendNewline()
				.append(Component.text("To skip upgrading,")).appendNewline().append(Component.text("shift click the paper!"));
			holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
		}
	}
	
	public void useUpgrade(UUID uuid) {
		notUsed.remove(uuid);
		if (notUsed.isEmpty()) {
			returnToNodes();
		}
	}
	
	public void returnToNodes() {
		s.broadcast("Everyone is ready! Returning you to node select...");
		state = RETURN_STATE;
		new BukkitRunnable() {
			public void run() {
				s.setInstance(new NodeSelectInstance(s));
				state = RETURN_FAIL_STATE; // Only used if we're still stuck in the room
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			ent.getValue().setInstanceData(notUsed.contains(ent.getKey()) ? "F" : "T");
		}
		return "CAMPFIRE:" + state;
	}

	@Override
	public void handlePlayerKickEvent(Player kicked) {
		notUsed.remove(kicked.getUniqueId());
		if (notUsed.isEmpty()) {
			returnToNodes();
		}
	}
}
