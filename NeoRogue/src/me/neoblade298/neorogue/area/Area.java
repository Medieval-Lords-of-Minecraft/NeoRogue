package me.neoblade298.neorogue.area;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;

public class Area {
	private AreaType type;
	private Node[][] nodes = new Node[MAX_POSITIONS][MAX_LANES];
	private Session s;
	public static World world;
	
	private static Clipboard clipboard;
	private static final int MIN_MINIBOSSES = 2;
	private static final int MIN_SHOPS = 3;
	private static final int MAX_LANES = 5;
	private static final int MAX_POSITIONS = 12;
	private static final int CENTER_LANE = MAX_LANES / 2;
	private static final double STRAIGHT_PATH_CHANCE = 0.7;
	private static final double DOUBLE_PATH_CHANCE = 0.6;
	public static final String WORLD_NAME = "Dev";
	private static final int X_EDGE_PADDING = 13, Z_EDGE_PADDING = 11, NODE_DIST_BETWEEN = 4;
	
	private static final int Z_OFFSET = 10;
	private static final int NODE_Y = 64;
	private static Location teleportBase;
	
	private Location teleport;
	
	// schematics
	private static String NODE_SELECT = "nodeselect.schem";
	
	// Offsets
	private int xOff, zOff;
	
	private ArrayList<Hologram> holograms = new ArrayList<Hologram>();
	
	public static void initialize() {
		world = BukkitAdapter.adapt(Bukkit.getWorld(WORLD_NAME));

		// Load the node select schematic
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, NODE_SELECT);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		teleportBase = new Location(Bukkit.getWorld(WORLD_NAME), -20.5, 0, 6.5);
	}
	
	public Area(AreaType type, int xOff, int zOff, Session s) {
		this.type = type;
		this.xOff = xOff;
		this.zOff = zOff + Z_OFFSET;
		this.teleport = teleportBase.clone().add(-this.xOff, 64, this.zOff);
		this.s = s;
		
		generateNodes();
	}

	// Deserialize
	public Area(AreaType type, int xOff, int zOff, UUID uuid, int saveSlot, Session s, Statement stmt) throws SQLException {
		this(type, xOff, zOff, s);
		
		ResultSet rs = stmt
				.executeQuery("SELECT * FROM neorogue_nodes WHERE uuid = '" + uuid + "' AND slot = " + saveSlot + ";");
		// First load the nodes themselves
		while (rs.next()) {
			String[] coords = rs.getString("node").split(",");
			int pos = Integer.parseInt(coords[0]);
			int lane = Integer.parseInt(coords[1]);
			nodes[pos][lane] = new Node(NodeType.valueOf(rs.getString("type")), pos, lane);
		}

		// Next load the node destinations now that they're populated
		rs.beforeFirst();
		while (rs.next()) {
			String[] coords = rs.getString("node").split(",");
			int pos = Integer.parseInt(coords[0]);
			int lane = Integer.parseInt(coords[1]);
			Node node = nodes[pos][lane];

			String[] dests = rs.getString("dests").split(" ");
			for (String dest : dests) {
				coords = dest.split(",");
				pos = Integer.parseInt(coords[0]);
				lane = Integer.parseInt(coords[1]);

				node.addDestination(nodes[pos][lane]);
			}
		}
	}
	
	private void generateNodes() {
		// Static nodes
		nodes[0][CENTER_LANE] = new Node(NodeType.START, 0, CENTER_LANE);
		nodes[1][CENTER_LANE - 2] = generateNode(true, 1, CENTER_LANE - 2);
		nodes[1][CENTER_LANE - 1] = generateNode(true, 1, CENTER_LANE - 1);
		nodes[1][CENTER_LANE] = generateNode(true, 1, CENTER_LANE);
		nodes[1][CENTER_LANE + 1] = generateNode(true, 1, CENTER_LANE + 1);
		nodes[1][CENTER_LANE + 2] = generateNode(true, 1, CENTER_LANE + 2);
		nodes[MAX_POSITIONS - 2][CENTER_LANE - 1] = new Node(NodeType.CAMPFIRE, MAX_POSITIONS - 2, CENTER_LANE - 1);
		nodes[MAX_POSITIONS - 2][CENTER_LANE] = new Node(NodeType.CAMPFIRE, MAX_POSITIONS - 2, CENTER_LANE);
		nodes[MAX_POSITIONS - 2][CENTER_LANE + 1] = new Node(NodeType.CAMPFIRE, MAX_POSITIONS - 2, CENTER_LANE + 1);
		nodes[MAX_POSITIONS - 1][CENTER_LANE] = new Node(NodeType.BOSS, MAX_POSITIONS - 1, CENTER_LANE);

		// Guaranteed minimums
		placeNodeRandomly(NodeType.MINIBOSS, MIN_MINIBOSSES);
		placeNodeRandomly(NodeType.SHOP, MIN_SHOPS);

		// Generate the rest randomly
		for (int pos = 2; pos < MAX_POSITIONS - 2; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				if (nodes[pos][lane] != null) continue;
				nodes[pos][lane] = generateNode(false, pos, lane);
			}
		}

		// Generate guaranteed paths
		nodes[0][CENTER_LANE].addDestination(nodes[1][CENTER_LANE - 2]);
		nodes[0][CENTER_LANE].addDestination(nodes[1][CENTER_LANE - 1]);
		nodes[0][CENTER_LANE].addDestination(nodes[1][CENTER_LANE]);
		nodes[0][CENTER_LANE].addDestination(nodes[1][CENTER_LANE + 1]);
		nodes[0][CENTER_LANE].addDestination(nodes[1][CENTER_LANE + 2]);
		nodes[MAX_POSITIONS - 2][CENTER_LANE - 1].addDestination(nodes[MAX_POSITIONS - 1][CENTER_LANE]);
		nodes[MAX_POSITIONS - 2][CENTER_LANE].addDestination(nodes[MAX_POSITIONS - 1][CENTER_LANE]);
		nodes[MAX_POSITIONS - 2][CENTER_LANE + 1].addDestination(nodes[MAX_POSITIONS - 1][CENTER_LANE]);

		// Generate remaining paths
		for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				if (nodes[pos][lane] == null) continue;
				generatePaths(pos, lane);
			}
		}

		// Delete all nodes that don't have sources
		for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node curr = nodes[pos][lane];
				if (curr == null) continue;
				
				deleteNodeWithoutSource(curr);
			}
		}
	}
	
	private void deleteNodeWithoutSource(Node node) {
		if (node.getSources().size() == 0) {
			nodes[node.getPosition()][node.getLane()] = null;
			for (Node dest : node.getDestinations()) {
				dest.removeSource(node);
				deleteNodeWithoutSource(dest);
			}
		}
	}

	private void generatePaths(int pos, int lane) {
		Node node = nodes[pos][lane];
		int numPaths = NeoCore.gen.nextDouble() < DOUBLE_PATH_CHANCE ? 2 : 1;

		ArrayList<Node> possiblePaths = new ArrayList<Node>(3);
		boolean hasStraightPath = false;
		if (nodes[pos + 1][lane] != null) {
			possiblePaths.add(nodes[pos + 1][lane]); // Straight path is always first
			hasStraightPath = true;
		}
		if (lane + 1 < MAX_LANES && nodes[pos + 1][lane + 1] != null) possiblePaths.add(nodes[pos + 1][lane + 1]);
		if (lane - 1 >= 0 && nodes[pos + 1][lane - 1] != null) possiblePaths.add(nodes[pos + 1][lane - 1]);

		if (possiblePaths.size() == 0) { // No nearby nodes, forcibly generate one as straight path
			Node dest = generateNode(true, pos + 1, lane);
			nodes[pos + 1][lane] = dest;
			node.addDestination(dest);
		}
		else if (possiblePaths.size() > numPaths) { // More nearby nodes than paths, choose which to path to
			if (hasStraightPath && NeoCore.gen.nextDouble() < STRAIGHT_PATH_CHANCE) {
				node.addDestination(possiblePaths.get(0));
			}
			else {
				if (numPaths == 1) {
					node.addDestination(possiblePaths.get(NeoCore.gen.nextInt(1, possiblePaths.size())));
				}
				else if (numPaths == 2) {
					node.addDestination(possiblePaths.get(1));
					node.addDestination(possiblePaths.get(2));
				}
			}
		}
		else { // Make a path to every node since we have <= nodes than paths
			for (Node dest : possiblePaths) {
				node.addDestination(dest);
			}
		}
	}

	private Node generateNode(boolean guaranteeNode, int pos, int lane) {
		/*
		 * Base chances: Fight 35%, Rest 5%, Miniboss 10%, Shop 5%, Event 25%, Nothing
		 * 20% Minimum Minibosses 2, Shops 3,
		 */
		double rand = NeoCore.gen.nextDouble();

		rand -= 0.35;
		if (rand < 0) return new Node(NodeType.FIGHT, pos, lane);
		rand -= 0.05;
		if (rand < 0) return new Node(NodeType.CAMPFIRE, pos, lane);
		rand -= 0.1;
		if (rand < 0) return new Node(NodeType.MINIBOSS, pos, lane);
		rand -= 0.05;
		if (rand < 0) return new Node(NodeType.SHOP, pos, lane);
		rand -= 0.25;
		if (rand < 0) return new Node(NodeType.CHANCE, pos, lane);

		return guaranteeNode ? new Node(NodeType.FIGHT, pos, lane) : null;
	}

	private void placeNodeRandomly(NodeType type, int count) {
		int pos, lane;
		for (int i = 0; i < count; i++) {
			do {
				pos = NeoCore.gen.nextInt(2, 10); // 2-9
				lane = NeoCore.gen.nextInt(5); // 0-4;
			} while (nodes[pos][lane] != null);

			nodes[pos][lane] = new Node(type, pos, lane);
		}
	}

	public AreaType getType() {
		return type;
	}

	public Node[][] getNodes() {
		return nodes;
	}

	public void save(UUID uuid, int saveSlot, Statement insert, Statement delete) throws SQLException {
		delete.addBatch("DELETE FROM neorogue_nodes WHERE uuid = '" + uuid + "' AND slot = " + saveSlot + ";");
		for (int pos = 1; pos < MAX_POSITIONS; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node node = nodes[pos][lane];
				// uuid, slot, node (0,0), dests (1,0 1,1), type
				insert.addBatch("INSERT INTO neorogue_nodes values('" + uuid + "'," + saveSlot + ",'" + node.toString()
						+ "','" + node.serializeDestinations() + "','" + node.getType());
			}
		}
	}

	public void instantiate() {
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
		    Operation operation = new ClipboardHolder(clipboard)
		            .createPaste(editSession)
		            .to(BlockVector3.at(xOff, 64, zOff))
		            .ignoreAirBlocks(false)
		            .build();
		    try {
				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		// Create nodes
		org.bukkit.World w = Bukkit.getWorld(WORLD_NAME);
		for (int lane = 0; lane < MAX_LANES; lane++) { // x
			for (int pos = 0; pos < MAX_POSITIONS; pos++) { // z
				Node node = nodes[pos][lane];
				if (node == null) continue;
				
				Location loc = new Location(w, -(xOff + X_EDGE_PADDING + (lane * NODE_DIST_BETWEEN)), NODE_Y, zOff + Z_EDGE_PADDING + (pos * NODE_DIST_BETWEEN));
				loc.getBlock().setType(node.getType().getBlock());
				loc.add(0, 0, -1);
				loc.getBlock().setType(Material.OAK_WALL_SIGN);
				Block b = loc.getBlock();
				Directional dir = (Directional) b.getBlockData();
				dir.setFacing(BlockFace.NORTH);
				b.setBlockData(dir);
				
				Sign sign = (Sign) b.getState();
				sign.setLine(1, "§l" + node.getType().toString());
				sign.setGlowingText(true);
				sign.update();
			}
		}
	}
	
	public Node getNodeFromLocation(Location loc) {
		int pos = loc.getBlockZ(), lane = loc.getBlockX();
		lane += xOff + X_EDGE_PADDING;
		lane /= NODE_DIST_BETWEEN;
		pos -= zOff + Z_EDGE_PADDING;
		pos /= NODE_DIST_BETWEEN;
		return nodes[pos][-lane];
	}
	
	// Called whenever a player advances to a new node
	public void update(Node node) {
		// Remove buttons and lecterns from old paths
		int pos = node.getPosition();
		for (int lane = 0; lane < 5; lane++) {
			Node src = nodes[node.getPosition()][lane];
			if (src == null) continue;
			Location loc = nodeToLocation(src, 1);
			System.out.println(loc);
			loc.getBlock().setType(Material.AIR);
			loc.add(0, -2, -1);
			loc.getBlock().setType(Material.POLISHED_ANDESITE);
		}

		// Delete holograms
		for (Hologram holo : holograms) {
			holo.delete();
		}

		// Add button to new paths and generate them
		for (Node dest : node.getDestinations()) {
			dest.generateInstance(s);
			
			Location loc = nodeToLocation(dest, 1);
			loc.getBlock().setType(Material.OAK_BUTTON);
			FaceAttachable face = (FaceAttachable) loc.getBlock().getBlockData();
			face.setAttachedFace(AttachedFace.FLOOR);
			loc.getBlock().setBlockData(face);

			// Add holograms to active nodes
			loc.add(0, 2, 0);
			Hologram holo = NeoRogue.holo.createHologram(loc);
			holo.getLines().appendText("§f§l" + dest.getType() + " Node");
			holograms.add(holo);
			
			// Fight nodes
			if (dest.getType() == NodeType.FIGHT) {
				loc.add(0, -4, -1);
				Block b = loc.getBlock();
				b.setType(Material.LECTERN);
				Lectern lec = (Lectern) b.getBlockData();
				lec.setFacing(BlockFace.NORTH);
				b.setBlockData(lec);

				ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
				BookMeta meta = (BookMeta) book.getItemMeta();
				meta.setAuthor("MLMC");
				meta.setTitle("Fight Info");
				org.bukkit.block.Lectern lc = (org.bukkit.block.Lectern) b.getState();
				lc.getInventory().addItem(book);
			}
		}
	}
	
	public void tickParticles(Player p, Node curr) {
		// Draw red lines for any locations that can immediately be visited
		for (Node dest : curr.getDestinations()) {
			ParticleUtil.drawLine(p, nodeToLocation(curr, 0.5), nodeToLocation(dest, 0.5),
					Particle.REDSTONE, true, 3, 0, 0, 0.3, new DustOptions(Color.RED, 1F));
		}
		
		// Draw black lines for locations past the immediate nodes
		for (int pos = curr.getPosition() + 1; pos < MAX_POSITIONS; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node node = nodes[pos][lane];
				if (node == null) continue;
				
				for (Node dest : node.getDestinations()) {
					ParticleUtil.drawLine(p, nodeToLocation(node, 0.5), nodeToLocation(dest, 0.5),
							Particle.REDSTONE, true, 3, 0, 0, 0.3, new DustOptions(Color.BLACK, 1F));
				}
			}
		}
	}
	
	public Location getTeleport() {
		return teleport;
	}
	
	private Location nodeToLocation(Node node, double yOff) {
		org.bukkit.World w = Bukkit.getWorld(WORLD_NAME);
		return new Location(w, -(xOff + X_EDGE_PADDING - 0.5 + (node.getLane() * 4)), NODE_Y + yOff, zOff + Z_EDGE_PADDING + 0.5 + (node.getPosition() * 4));
	}
}
