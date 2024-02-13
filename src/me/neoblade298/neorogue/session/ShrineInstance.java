package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class ShrineInstance extends EditInventoryInstance {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORKS_SPARK).count(50).spread(2, 2).speed(0.1);
	private static final double SPAWN_X = Session.SHRINE_X + 5.5, SPAWN_Z = Session.SHRINE_Z + 2.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 7;
	private static final int INIT_STATE = 0, REST_STATE = 1, UPGRADE_STATE = 2, RETURN_STATE = 3, RETURN_FAIL_STATE = 4;
	private int state = 0;
	private Block blockBottom, blockMiddle, blockTop;
	private HashSet<UUID> notUsed = new HashSet<UUID>();
	private Hologram holo;
	
	public ShrineInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
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
	public void start() {
		blockBottom = spawn.clone().add(0, 0, 7).getBlock();
		blockMiddle = blockBottom.getRelative(BlockFace.UP);
		blockTop = blockMiddle.getRelative(BlockFace.UP);
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			notUsed.add(p.getUniqueId());
			p.teleport(spawn);
		}

		// Setup hologram
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Right click the");
		lines.add("§aemerald blocks§f!");
		Plot plot = s.getPlot();
		holo = DHAPI.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-shrine", spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), lines);
	}

	@Override
	public void cleanup() {
		blockBottom.setType(Material.EMERALD_BLOCK);
		blockMiddle.setType(Material.EMERALD_BLOCK);
		blockTop.setType(Material.AIR);
		holo.delete();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);

		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (state == RETURN_FAIL_STATE) {
			returnToNodes();
			return;
		}
		
		if (e.getClickedBlock().getType() == Material.EMERALD_BLOCK && state == INIT_STATE) {
			if (!s.getHost().equals(uuid)) {
				Util.displayError(p, "The host must first choose what to do!");
				return;
			}
			
			// open host inventory
			new ShrineChoiceInventory(p, this);
			return;
		}

		if (e.getClickedBlock().getType() == Material.ANVIL && notUsed.contains(uuid) && state == UPGRADE_STATE) {
			new ShrineUpgradeInventory(p, this);
		}
	}

	public void chooseState(boolean rest) {
		state = rest ? REST_STATE : UPGRADE_STATE;
		s.broadcast("The host has chosen to <yellow>" + (rest ? "rest" : "upgrade"));
		part.spawn(blockMiddle.getLocation());
		s.broadcastSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);

		Location loc = holo.getLocation();
		holo.delete();
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
			
			
			returnToNodes();
		}
		else {
			blockBottom.setType(Material.REINFORCED_DEEPSLATE);
			blockMiddle.setType(Material.ANVIL);
			Directional anvil = (Directional) blockMiddle.getBlockData();
			anvil.setFacing(BlockFace.EAST);
			blockMiddle.setBlockData(anvil);
			blockTop.setType(Material.WITHER_SKELETON_SKULL);

			ArrayList<String> lines = new ArrayList<String>();
			loc.add(0, 1, 0);
			lines.add("Use the anvil!");
			Plot plot = s.getPlot();
			holo = DHAPI.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-shrineanvil", spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), lines);
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
	public void teleportPlayer(Player p) {
		p.teleport(spawn);
		returnToNodes();
	}
}
