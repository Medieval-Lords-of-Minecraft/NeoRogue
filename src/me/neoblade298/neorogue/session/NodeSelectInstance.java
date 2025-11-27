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
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.FightInfoInventory;
import me.neoblade298.neorogue.region.Node;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class NodeSelectInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.AREA_X + 21.5, SPAWN_Z = Session.AREA_Z + 6.5;
	private BukkitTask task;
	private ArrayList<TextDisplay> holograms = new ArrayList<TextDisplay>();
	
	private static final ArrayList<Component> tips = new ArrayList<Component>();
	
	static {
		tips.add(Component.text("Shrines can be used to heal").appendNewline().append(Component.text("or upgrade one of your equipment!")));
		tips.add(Component.text("Keep an eye out for equipment").appendNewline().append(Component.text("that can be reforged with your"))
			.appendNewline().append(Component.text("existing equipment to get even stronger!")));
	}

	public NodeSelectInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}

	public NodeSelectInstance(Session s, HashMap<UUID, PlayerSessionData> party) {
		this(s);
	}
	
	@Override
	public void setup() {
		Region region = s.getRegion();

		// Teleport player to their previous node selection
		if (s.getNode().getRow() != 0)
			spawn = region.nodeToLocation(s.getNode(), 1);
		region.update(s.getNode(), this);

		// Set up boss hologram and tips
		Component text = Component.text("Boss: ", NamedTextColor.WHITE, TextDecoration.BOLD).append(Component.text(region.getBoss(), NamedTextColor.RED, TextDecoration.BOLD));
		text = text.decoration(TextDecoration.BOLD, State.FALSE).appendNewline().append(tips.get(NeoRogue.gen.nextInt(tips.size())));
		Location loc = spawn.clone().add(0, 2.8, 4);
		TextDisplay holo = NeoRogue.createHologram(loc, text);
		holograms.add(holo);

		for (Player p : s.getOnlinePlayers()) {
			teleportRandomly(p);
			p.setAllowFlight(true);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
			p.setAllowFlight(true);
		}
		super.setup();
		task = new BukkitRunnable() {
			@Override
			public void run() {
				region.tickParticles(s.getNode());
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
		Component text = Component.text(dest.getType() + " Node", NamedTextColor.WHITE, TextDecoration.BOLD);
		TextDisplay holo = NeoRogue.createHologram(loc, text);
		holograms.add(holo);
	}

	@Override
	public void updateBoardLines() {
		// Empty as node select board is per player
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		task.cancel();
		
		// Regular players have flight removed when fight starts, spectatrs don't need this since they're invulnerable
		for (UUID uuid : s.getSpectators().keySet()) {
			Bukkit.getPlayer(uuid).setAllowFlight(false);
		}

		for (TextDisplay holo : holograms) {
			holo.remove();
		}
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getRegion().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), null, inst.getMap().getMobs(), inst.getMap().hasCustomMobInfo());
		} else {
			super.handleSpectatorInteractEvent(e);
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;

		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && Tag.BUTTONS.isTagged(e.getClickedBlock().getType())) {
			Node node = s.getRegion().getNodeFromLocation(e.getClickedBlock().getLocation());
			if (node == null)
				return;
			if (!p.getUniqueId().equals(s.getHost())) {
				if (!s.canSuggest())
					return;
				s.setSuggestCooldown();
				String laneString;
				switch (node.getLane()) {
				case 0:
					laneString = " on the far left.";
					break;
				case 1:
					laneString = " on the middle left.";
					break;
				case 2:
					laneString = " in the middle.";
					break;
				case 3:
					laneString = " on the middle right.";
					break;
				default:
					laneString = " on the far right.";
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

				for (UUID uuid : s.getSpectators().keySet()) {
					Bukkit.getPlayer(uuid).setAllowFlight(false);
				}
			} else {
				s.setBusy(true);
			}
			return;
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.LECTERN) {
			e.setCancelled(true);
			Node n = s.getRegion().getNodeFromLocation(e.getClickedBlock().getLocation().add(0, 2, 1));
			FightInstance inst = (FightInstance) n.getInstance();
			new FightInfoInventory(e.getPlayer(), s.getParty().get(p.getUniqueId()), inst.getMap().getMobs(), inst.getMap().hasCustomMobInfo());
		} else {
			super.handleInteractEvent(e);
		}
	}
	
	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return "NODESELECT";
	}

	@Override
	public void handlePlayerLeaveParty(Player p) {
		
	}
}
