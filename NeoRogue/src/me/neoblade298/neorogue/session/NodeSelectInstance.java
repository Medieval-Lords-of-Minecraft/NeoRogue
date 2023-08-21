package me.neoblade298.neorogue.session;

import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.player.FightInfoInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class NodeSelectInstance implements Instance {
	private Session s;
	private BukkitTask task;
	
	public NodeSelectInstance(Session s) {
		this.s = s;
	}

	@Override
	public void start(Session s) {
		Area area = s.getArea();
		area.update(s.getNode());
		task = new BukkitRunnable() {
			public void run() {
				for (Player p : s.getOnlinePlayers()) {
					area.tickParticles(p, s.getNode());
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 20L);
	}

	@Override
	public void cleanup() {
		task.cancel();
	}
	
	public void handleRightClick(PlayerInteractEvent e) {
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
				
				if (!ent.getValue().saveStorage()) {
					for (Player online : s.getOnlinePlayers()) {
						Util.displayError(online, "&&4" + ent.getValue().getData().getDisplay() + "&c has too many items in their inventory! They must drop some "
								+ "to satisfy their storage limit of &e" + ent.getValue().getMaxStorage() + "&c!");
					}
					return;
				}
			}
			s.setInstance(s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation()).getInstance());
			return;
		}
		else if (e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getArea().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), inst.getMap().getMobs());
		}
	}
}
