package me.neoblade298.neorogue.session;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.area.NodeType;
import me.neoblade298.neorogue.player.MapViewer;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class EditInventoryInstance extends Instance {
	public static final int MAP_ID = 256;
	public EditInventoryInstance(Session s, double spawnX, double spawnZ) {
		super(s, spawnX, spawnZ);
	}
	
	@Override
	public void start() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.setupInventory();
			data.updateBoardLines();
		}
		
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			s.setupSpectatorInventory(p);
		}
	}

	public static boolean isValid(Session s) {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			if (data.hasUnequippedCurses()) {
				s.broadcastError(data.getData().getDisplay() + " must equip all their curses before continuing!");
				return false;
			}
			if (data.exceedsStorageLimit()) {
				s.broadcastError(data.getData().getDisplay() + " must remove some equipment before continuing!");
				return false;
			}
			
			if (data.getPlayer() != null && InventoryListener.hasOpenCoreInventory(p)) {
				s.broadcastError(data.getData().getDisplay() + " must close their inventory before continuing!");
				return false;
			}
		}
		return true;
	}
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Session s = SessionManager.getSession(p);
		if (s.getInstance() instanceof NodeSelectInstance) return;
		PlayerSessionData psd = s.getData(p.getUniqueId());
		if (psd.isViewingMap()) {
			psd.stopViewingMap();
		}
	}

	public static void handleSwapHand(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		Session s = SessionManager.getSession(p);
		if (s.isBusy() || s.getInstance() instanceof NodeSelectInstance) {
			e.setCancelled(true);
			return;
		}
		PlayerSessionData psd = s.getData(p.getUniqueId());
		if (psd.isViewingMap()) {
			// Can't use data.stopViewingMap() because the event overrides inventory changes
			Sounds.turnPage.play(p, p);
			e.setMainHandItem(psd.getHiddenMapItem());
			psd.setHiddenMapItem(null);
		}
		else {
			Sounds.turnPage.play(p, p);
			psd.setHiddenMapItem(e.getOffHandItem());
			psd.setMapSlot(e.getPlayer().getInventory().getHeldItemSlot());
			e.setOffHandItem(null);
		}
		psd.setViewingMap(!psd.isViewingMap());
	}

	@Override
	public void cleanup() {
		for (PlayerSessionData data : s.getParty().values()) {
			if (data.isViewingMap()) {
				data.stopViewingMap();
			}
		}

		for (MapViewer viewer : s.getSpectators().values()) {
			Player p = Bukkit.getPlayer(viewer.getUniqueId());
			if (p != null) p.getInventory().clear();
		}
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		if (this instanceof NodeSelectInstance) return;
		if (e.getAction().isLeftClick()) {
			s.getSpectator(e.getPlayer().getUniqueId()).scrollMapDown();
		}
		else if (e.getAction().isRightClick()) {
			s.getSpectator(e.getPlayer().getUniqueId()).scrollMapUp();
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (this instanceof NodeSelectInstance) return;
		if (e.getAction().isLeftClick()) {
			s.getParty().get(e.getPlayer().getUniqueId()).scrollMapDown();
		}
		else if (e.getAction().isRightClick()) {
			s.getParty().get(e.getPlayer().getUniqueId()).scrollMapUp();
		}
	}

	public static class NodeMapRenderer extends MapRenderer {
		private static final int MAP_X_SLOTS[] = new int[] { -100, -50, -1, 50, 100 },
			MAP_Y_SLOTS[] = new int[] { 80, 40, 0, -40, -80 };

		public NodeMapRenderer() {
			super(true);
		}

		@Override
		public void render(MapView view, MapCanvas canvas, Player p) {
			Session s = SessionManager.getSession(p);
			if (s == null) return;
			UUID uuid = p.getUniqueId();
			MapViewer viewer = null;
			if (s.isSpectator(uuid)) {
				viewer = s.getSpectator(uuid);
			}
			else if (s.getData(uuid) != null) {
				viewer = s.getData(uuid);
			}

			if (viewer == null) return;

			if (viewer.shouldRenderMap()) {
				viewer.setShouldMapRender(false);
				MapCursorCollection col = new MapCursorCollection();
				Area a = s.getArea();
				int mapPos = viewer.getMapPosition();

				for (int i = 0; i < 256; i++) {
					for (int j = 0; j < 256; j++) {
						canvas.setPixelColor(i, j, java.awt.Color.white);
					}
				}

				Node[][] nodes = a.getNodes();
				for (int lane = 0; lane < Area.MAX_LANES; lane++) {
					for (int pos = mapPos; pos < Area.MAX_POSITIONS && pos < mapPos + MAP_Y_SLOTS.length; pos++) {
						Node n = nodes[pos][lane];
						if (n == null) continue;
						byte x = (byte) MAP_X_SLOTS[lane];
						byte y = (byte) MAP_Y_SLOTS[pos - mapPos];
						boolean isCurr = n.equals(s.getNode());

						MapCursor curs;
						if (isCurr) {
							curs = new MapCursor(x, y, (byte) 0, MapCursor.Type.PLAYER, true, Component.text("You are here"));
						}
						else if (n.getType() == NodeType.BOSS) {
							curs = new MapCursor(x, y, (byte) 0, n.getType().getCursor(), true, 
								Component.text(((BossFightInstance) n.getInstance()).getBossDisplay(), NamedTextColor.WHITE, TextDecoration.BOLD));
						}
						else {
							curs = new MapCursor(x, y, (byte) 0, n.getType().getCursor(), true, Component.text(n.getType().name()));
						}
						col.addCursor(curs);

						// Only draw paths for visible nodes
						if (pos + 1 >= mapPos + MAP_Y_SLOTS.length) continue;
						for (Node dest : n.getDestinations()) {
							drawLine(canvas, MAP_X_SLOTS[lane], MAP_Y_SLOTS[pos - mapPos], MAP_X_SLOTS[dest.getLane()],
								MAP_Y_SLOTS[dest.getPosition() - mapPos], n.equals(s.getNode()) ? java.awt.Color.red : java.awt.Color.black);
						}
					}
				}
				canvas.setCursors(col);
				canvas.drawText(2, 0, MinecraftFont.Font, "Left/Right click to scroll");
			}
		}


		private void drawLine(MapCanvas canvas, int x1, int y1, int x2, int y2, java.awt.Color color) {
			// Convert from byte locations to pixel locations, also add padding
			int lowX = (Math.min(x1, x2) / 2) + 63;
			int lowY = (Math.min(y1, y2) / 2) + 64 + 4;
			int highX = (Math.max(x1, x2) / 2) + 63;
			int highY = (Math.max(y1, y2) / 2) + 64 - 4;
			int dx = highX - lowX;
			int width = dx / 25;
			boolean left = x1 > x2;
			for (int y = lowY; y < highY; y++) {
				int pct = (y - lowY) * 100 / (highY - lowY);
				if (!left) pct = -pct;
				int x = (((left ? lowX : highX) * 100) + (dx * pct)) / 100;
				for (int xOff = -width; xOff <= width; xOff++) {
					canvas.setPixelColor(x + xOff, y, color);
				}
			}
		}
	}
}
