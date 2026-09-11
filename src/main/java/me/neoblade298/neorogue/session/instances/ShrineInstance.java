package me.neoblade298.neorogue.session.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.shrine.ShrineChoiceInventory;
import me.neoblade298.neorogue.session.shrine.ShrineUpgradeInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShrineInstance extends EditInventoryInstance {
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).count(50).spread(2, 2).speed(0.1);
	private static final double SPAWN_X = Session.SHRINE_X + 5.5, SPAWN_Z = Session.SHRINE_Z + 2.5,
			HOLO_X = 0, HOLO_Y = 2.5, HOLO_Z = 7;
	private static final String UNDECIDED = "N", UPGRADE_PENDING = "U", COMPLETE = "D";
	private static final int UPGRADE_STATE = 2, RETURNING_STATE = 3, RETURN_FAIL_STATE = 4;
	private int state = 0;
	private Block blockBottom, blockMiddle, blockTop;
	private HashSet<UUID> notUsed = new HashSet<UUID>();
	private HashSet<UUID> pendingUpgrade = new HashSet<UUID>();
	private TextDisplay holo;
	private boolean deserialized = false;
	
	public ShrineInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
		spectatorLines = playerLines;
	}
	
	public ShrineInstance(Session s, String data, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		deserialized = true;
		int savedState = Integer.parseInt(data.substring(data.length() - 1));
		if (savedState == RETURNING_STATE || savedState == RETURN_FAIL_STATE) state = savedState;
		
		for (PlayerSessionData pd : party.values()) {
			UUID uuid = pd.getUniqueId();
			String playerState = pd.getInstanceData();
			if (UNDECIDED.equals(playerState) || ("F".equals(playerState) && savedState != UPGRADE_STATE)) {
				notUsed.add(uuid);
			}
			else if (UPGRADE_PENDING.equals(playerState) || ("F".equals(playerState) && savedState == UPGRADE_STATE)) {
				notUsed.add(uuid);
				pendingUpgrade.add(uuid);
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
			// A fresh shrine starts with everyone yet to use it; a deserialized shrine already has its
			// notUsed set restored from the save, so don't clobber it here.
			if (!deserialized) notUsed.add(p.getUniqueId());
			teleportRandomly(p);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();

		Component text = Component.text("Right click the").appendNewline().append(Component.text("emerald blocks", NamedTextColor.GREEN)).append(Component.text("!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
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
		UUID uuid = data.getUniqueId();
		String line = !notUsed.contains(uuid) ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay() + " (" + Math.round(data.getHealth()) + " / " + (int) data.getMaxHealth()
				+ "§c♥§f)";
		return line;
	}

	@Override
	public Component getActionBar(PlayerSessionData data) {
		UUID uuid = data.getUniqueId();
		if (pendingUpgrade.contains(uuid)) return getActionBar(data, "Choose an Upgrade", NamedTextColor.YELLOW);
		boolean isReady = !notUsed.contains(uuid);
		return getActionBar(data, isReady ? "Ready" : "Choose Rest or Upgrade",
				isReady ? NamedTextColor.GREEN : NamedTextColor.RED);
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		blockBottom.setType(Material.EMERALD_BLOCK);
		blockMiddle.setType(Material.EMERALD_BLOCK);
		blockTop.setType(Material.AIR);
		if (holo != null) holo.remove();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);

		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			if (state == RETURN_FAIL_STATE || (state != RETURNING_STATE && notUsed.isEmpty())) {
				returnToNodes();
				return;
			}
			
			if (e.getClickedBlock().getType() == Material.EMERALD_BLOCK && notUsed.contains(uuid)) {
				if (pendingUpgrade.contains(uuid)) {
					new ShrineUpgradeInventory(p, s.getData(uuid), this);
				}
				else {
					new ShrineChoiceInventory(p, s.getParty().get(uuid), this);
				}
				return;
			}
			super.handleInteractEvent(e);
		}
		else {
			super.handleInteractEvent(e);
		}
	}
	
	public void chooseState(Player p, boolean rest) {
		UUID uuid = p.getUniqueId();
		if (!notUsed.contains(uuid) || pendingUpgrade.contains(uuid)) return;
		PlayerSessionData data = s.getData(uuid);
		if (rest) data.markRestedAtShrine();
		else data.markUpgradedAtShrine();
		part.play(blockMiddle.getLocation());
		p.playSound(p, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1F, 1F);
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		if (rest) {
			data.healPercent(0.35);
			notUsed.remove(uuid);
			finishIfReady();
		}
		else {
			pendingUpgrade.add(uuid);
		}
		updateBoardLines();
	}
	
	public void useUpgrade(UUID uuid) {
		pendingUpgrade.remove(uuid);
		notUsed.remove(uuid);
		updateBoardLines();
		finishIfReady();
	}

	private void finishIfReady() {
		if (notUsed.isEmpty()) returnToNodes();
	}
	
	public void returnToNodes() {
		new BukkitRunnable() {
			public void run() {
				NodeSelectInstance next = NodeSelectInstance.create(s);
				if (!s.canSetInstance(next)) return;
				s.broadcast("Returning to node select...");
				state = RETURNING_STATE;
				new BukkitRunnable() {
					public void run() {
						if (s.getInstance() != ShrineInstance.this) return;
						if (!s.setInstance(next))
							state = RETURN_FAIL_STATE;
					}
				}.runTaskLater(NeoRogue.inst(), 60L);
			}
		}.runTaskLater(NeoRogue.inst(), 1);
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			UUID uuid = ent.getKey();
			String playerState = pendingUpgrade.contains(uuid) ? UPGRADE_PENDING
					: notUsed.contains(uuid) ? UNDECIDED : COMPLETE;
			ent.getValue().setInstanceData(playerState);
		}
		return InstanceType.SHRINE.prefix() + state;
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {
		UUID uuid = p.getUniqueId();
		pendingUpgrade.remove(uuid);
		notUsed.remove(uuid);
		finishIfReady();
	}
}
