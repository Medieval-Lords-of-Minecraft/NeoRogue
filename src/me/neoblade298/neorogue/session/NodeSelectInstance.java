package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.FightInfoInventory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NodeSelectInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.AREA_X + 21.5, SPAWN_Z = Session.AREA_Z + 6.5;
	private BukkitTask task;
	private ArrayList<Hologram> holograms = new ArrayList<Hologram>();
	
	private static final ArrayList<ArrayList<String>> tips = new ArrayList<ArrayList<String>>();
	
	static {
		ArrayList<String> tip = new ArrayList<String>();
		tip.add("Shrines can be used to heal");
		tip.add("or upgrade one or your equipment!");
		tips.add(tip);

		tip = new ArrayList<String>();
		tip.add("Keep an eye out for equipment");
		tip.add("that can be reforged with your");
		tip.add("existing equipment to get even stronger!");
		tips.add(tip);
	}

	public NodeSelectInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}

	public NodeSelectInstance(Session s, HashMap<UUID, PlayerSessionData> party) {
		this(s);
	}
	
	@Override
	public void start() {
		Area area = s.getArea();

		// Teleport player to their previous node selection
		if (s.getNode().getPosition() != 0)
			spawn = area.nodeToLocation(s.getNode(), 1);
		area.update(s.getNode(), this);

		// Set up boss hologram and tips
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("§f§lBoss: §4§l" + area.getBoss());
		lines.addAll(tips.get(NeoRogue.gen.nextInt(tips.size())));
		Plot plot = s.getPlot();
		Location loc = spawn.clone().add(0, 2.5 + (lines.size() * 0.3), 4);
		Hologram holo = NeoRogue.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-bossdisplay", loc, lines);
		holograms.add(holo);

		for (Player p : s.getOnlinePlayers()) {
			p.teleport(spawn);
			p.setAllowFlight(true);
		}
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
			p.setAllowFlight(true);
		}
		super.start();
		task = new BukkitRunnable() {
			@Override
			public void run() {
				area.tickParticles(s.getNode());
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 15L);
	}

	@Override
	public void handlePlayerRejoin(Player p) {
		super.handlePlayerRejoin(p);
		p.setAllowFlight(true);
	}

	@Override
	public void handlePlayerLeave(Player p) {
		p.setAllowFlight(false);
	}

	public void createHologram(Location loc, Node dest) {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("§f§l" + dest.getType() + " Node");
		Plot plot = s.getPlot();
		Hologram holo = NeoRogue.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-" + dest.getPosition() + "-" + dest.getLane(), loc, lines);
		holograms.add(holo);
	}
	
	@Override
	public void cleanup() {
		task.cancel();
		
		// Regular players have flight removed when fight starts, spectatrs don't need this since they're invulnerable
		for (UUID uuid : s.getSpectators()) {
			Bukkit.getPlayer(uuid).setAllowFlight(false);
		}

		for (Hologram holo : holograms) {
			holo.delete();
		}
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		if (e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), null, inst.getMap().getMobs());
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		Player p = e.getPlayer();
		Node node = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation());
		if (Tag.BUTTONS.isTagged(e.getClickedBlock().getType())) {
			if (!p.getUniqueId().equals(s.getHost())) {
				if (!s.canSuggest()) return;
				s.setSuggestCooldown();
				String laneString;
				switch (node.getLane()) {
				case 0: laneString = " on the far left.";
				break;
				case 1: laneString = " on the middle left.";
				break;
				case 2: laneString = " in the middle.";
				break;
				case 3: laneString = " on the middle right.";
				break;
				default: laneString = " on the far right.";
				break;
				}
				s.broadcast(
						p.name().color(NamedTextColor.YELLOW)
						.append(Component.text(" suggests the ", NamedTextColor.GRAY))
						.append(Component.text(node.getType() + " Node", NamedTextColor.YELLOW))
						.append(Component.text(laneString, NamedTextColor.GRAY))
					);
					s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
				return;
			}

			// Validation
			if (!s.isEveryoneOnline())
				return;
			if (s.setInstance(node.getInstance()))
				s.visitNode(node);
			if (!(node.getInstance() instanceof FightInstance)) {
				for (Player pl : s.getOnlinePlayers()) {
					pl.setAllowFlight(false);
				}

				for (UUID uuid : s.getSpectators()) {
					Bukkit.getPlayer(uuid).setAllowFlight(false);
				}
			}
			else {
				s.setBusy(true);
			}
			// Fight instances set allow flight to false and set busy to false on start
			return;
		} else if (e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), s.getParty().get(p.getUniqueId()), inst.getMap().getMobs());
		}
	}
	
	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return "NODESELECT";
	}

	@Override
	public void handlePlayerKickEvent(Player kicked) {
		
	}
}
