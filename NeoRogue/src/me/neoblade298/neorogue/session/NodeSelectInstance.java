package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.FightInfoInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class NodeSelectInstance extends EditInventoryInstance {
	private BukkitTask task;
	private ArrayList<Hologram> holograms = new ArrayList<Hologram>();
	
	public NodeSelectInstance() {}
	
	public NodeSelectInstance(HashMap<UUID, PlayerSessionData> party) {
		
	}

	@Override
	public void start(Session s) {
		this.s = s;
		Area area = s.getArea();
		area.update(s.getNode(), this);
		
		spawn = area.getTeleport();
		for (Player p : s.getOnlinePlayers()) {
			p.teleport(spawn);
		}
		task = new BukkitRunnable() {
			public void run() {
				for (Player p : s.getOnlinePlayers()) {
					area.tickParticles(p, s.getNode());
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 20L);
	}
	
	public void createHologram(Location loc, Node dest) {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("§f§l" + dest.getType() + " Node");
		Hologram holo = DHAPI.createHologram(s.getPlot() + "-" + dest.getPosition(), loc, lines);
		holograms.add(holo);
	}

	@Override
	public void cleanup() {
		task.cancel();
		
		for (Hologram holo : holograms) {
			holo.delete();
		}
	}
	
	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player p = e.getPlayer();
		if (Tag.BUTTONS.isTagged(e.getClickedBlock().getType())) {
			if (!p.getUniqueId().equals(s.getHost())) {
				Util.displayError(p, "&cOnly the host may choose the next node to visit!");
				return;
			}
			
			// Validation
			for (Entry<UUID, PlayerSessionData> ent : s.getParty().entrySet()) {
				Player member = ent.getValue().getPlayer();
				if (member == null) {
					for (Player online : s.getOnlinePlayers()) {
						Util.displayError(online, "&cAt least one party member (&4" + ent.getValue().getData().getDisplay() + "&c) is not online!");
					}
					return;
				}
			}
			if (!s.isEveryoneOnline()) return;
			Node node = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation());
			s.setNode(node);
			s.setInstance(node.getInstance());
			return;
		}
		else if (e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), inst.getMap().getMobs());
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return "NODESELECT";
	}
}
