package me.neoblade298.neorogue.area;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neocore.shared.droptables.DropTable;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class Area {
	private static final int MAX_LANES = 5, MAX_POSITIONS = 16, CENTER_LANE = MAX_LANES / 2;
	private static final int X_EDGE_PADDING = 14, Z_EDGE_PADDING = 11, NODE_DIST_BETWEEN = 4;
	private static final int[] GENERATE_ORDER = new int[] {0, 4, 1, 3, 2};
	private static ParticleContainer red = new ParticleContainer(Particle.REDSTONE), black;
	private static HashMap<Integer, DropTable<Integer>> pathChances = new HashMap<Integer, DropTable<Integer>>();

	private AreaType type;
	private Node[][] nodes = new Node[MAX_POSITIONS][MAX_LANES];
	private Session s;
	private String boss;

	public static World world;
	public static final String WORLD_NAME = "Dev";
	private static final int NODE_Y = 64;

	// Offsets
	private int xOff, zOff;
	

	public static void initialize() {
		world = BukkitAdapter.adapt(Bukkit.getWorld(WORLD_NAME));

		// Load particles
		red.count(3).spread(0.1, 0.1).ignoreSettings(true).dustOptions(new DustOptions(Color.RED, 1F));
		black = red.clone().dustOptions(new DustOptions(Color.BLACK, 1F));
		
		// Load path chances
		DropTable<Integer> paths = new DropTable<Integer>();
		paths.add(2, 1);
		paths.add(3, 7);
		paths.add(4, 2);
		pathChances.put(2, paths);
		
		paths = new DropTable<Integer>();
		paths.add(2, 1);
		paths.add(3, 6);
		paths.add(4, 1);
		paths.add(5, 1);
		pathChances.put(3, paths);
		
		paths = new DropTable<Integer>();
		paths.add(2, 1);
		paths.add(3, 4);
		paths.add(4, 4);
		paths.add(5, 1);
		pathChances.put(4, paths);
		
		paths = new DropTable<Integer>();
		paths.add(3, 5);
		paths.add(4, 4);
		paths.add(5, 1);
		pathChances.put(5, paths);
	}
	
	public Area() {}

	public Area(AreaType type, int xOff, int zOff, Session s) {
		this.type = type;
		this.xOff = xOff;
		this.zOff = zOff + Session.AREA_Z;
		this.s = s;

		generateNodes();

		// Should only save all nodes at first, on auto-save only save nodes within
		// reach (for instance data)
		new BukkitRunnable() {
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-Area");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()) {
					saveAll(insert, delete);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	// Deserialize
	public Area(AreaType type, int xOff, int zOff, UUID uuid, int saveSlot, Session s, Statement stmt)
			throws SQLException {
		this.type = type;
		this.xOff = xOff;
		this.zOff = zOff + Session.AREA_Z;
		this.s = s;

		ResultSet rs = stmt
				.executeQuery("SELECT * FROM neorogue_nodes WHERE host = '" + uuid + "' AND slot = " + saveSlot + ";");
		// First load the nodes themselves
		while (rs.next()) {
			int pos = rs.getInt("position");
			int lane = rs.getInt("lane");
			nodes[pos][lane] = new Node(NodeType.valueOf(rs.getString("type")), pos, lane);
			nodes[pos][lane].deserializeInstance(s, rs.getString("instanceData"));
		}

		// Next load the node destinations now that they're populated
		// have to redo the statement since resultsets are type forward only
		rs = stmt.executeQuery("SELECT * FROM neorogue_nodes WHERE host = '" + uuid + "' AND slot = " + saveSlot + ";");
		while (rs.next()) {
			int pos = rs.getInt("position");
			int lane = rs.getInt("lane");
			Node node = nodes[pos][lane];

			String[] dests = rs.getString("destinations").split(" ");
			for (String dest : dests) {
				if (dest.isBlank()) continue;
				String[] coords = dest.split(",");
				pos = Integer.parseInt(coords[0]);
				lane = Integer.parseInt(coords[1]);

				node.addDestination(nodes[pos][lane]);
			}
		}
	}

	private void generateNodes() {
		// Static nodes
		nodes[0][CENTER_LANE] = new Node(NodeType.START, 0, CENTER_LANE);
		
		// Generate 2-5 starting positions
		int[] arr = new int[] {0, 1, 2, 3, 4};
		shuffleArray(arr);
		for (int i = 0; i < NeoCore.gen.nextInt(2, 6); i++) { // 5 is exclusive
			nodes[1][arr[i]] = generateNode(GenerationType.INITIAL, 1, arr[i], nodes[0][CENTER_LANE]);
		}
		
		nodes[MAX_POSITIONS - 2][CENTER_LANE - 1] = new Node(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE - 1);
		nodes[MAX_POSITIONS - 2][CENTER_LANE] = new Node(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE);
		nodes[MAX_POSITIONS - 2][CENTER_LANE + 1] = new Node(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE + 1);
		nodes[MAX_POSITIONS - 1][CENTER_LANE] = new Node(NodeType.BOSS, MAX_POSITIONS - 1, CENTER_LANE);
		nodes[MAX_POSITIONS - 1][CENTER_LANE].generateInstance(s, type); // generate boss
		BossFightInstance bi = (BossFightInstance) nodes[MAX_POSITIONS - 1][CENTER_LANE].getInstance();
		boss = bi.getBossDisplay();

		// Start generating by position
		for (int pos = 2; pos < MAX_POSITIONS - 2; pos++) {
			GenerationType type = (pos >= 5 && pos <= 7) || (pos >= 11 && pos <= 13) ? GenerationType.SPECIAL : GenerationType.NORMAL;
			nodes[pos] = generatePosition(type, nodes[pos - 1]);
		}
		
		// Connect generated nodes to static nodes
		for (int i = 0; i < 5; i++) {
			if (nodes[MAX_POSITIONS - 3][i] == null) continue;
			Node node = nodes[MAX_POSITIONS - 3][i];
			if (i == 0 || i == 1) {
				node.addDestination(nodes[MAX_POSITIONS - 2][1]);
			}
			else if (i == 3 || i == 4) {
				node.addDestination(nodes[MAX_POSITIONS - 2][3]);
			}
			else {
				node.addDestination(nodes[MAX_POSITIONS - 2][2]);
			}
		}
		
		// Connect campfires to boss
		for (int i = 1; i < 4; i++) {
			Node node = nodes[MAX_POSITIONS - 2][i];
			if (node.getSources().isEmpty()) {
				nodes[MAX_POSITIONS - 2][i] = null;
				continue;
			}
			nodes[MAX_POSITIONS - 2][i].addDestination(nodes[MAX_POSITIONS - 1][CENTER_LANE]);
		}
	}
	
	public String getBoss() {
		return boss;
	}
	
	private Node[] generatePosition(GenerationType type, Node[] prevPos) {
		Node[] curr = new Node[MAX_POSITIONS];
		LinkedList<Integer> prevNodeLanes = new LinkedList<Integer>();
		for (Node node : prevPos) {
			if (node != null) prevNodeLanes.add(node.getLane());
		}
		int toGenerate = pathChances.get(prevNodeLanes.size()).get(); // Number of nodes we want to generate
		int nodeDiff = toGenerate - prevNodeLanes.size();
		if (nodeDiff > 0) {
			// First pick the nodes to add two dests
			if (prevNodeLanes.size() == 3 && toGenerate == 5) {
				// Edge case, if there are 3 nodes, you must choose the edge nodes
				prevNodeLanes.remove(1);
			}
			// Edge case 123->4, cannot choose middle
			else if (prevNodeLanes.size() == 3 && prevNodeLanes.getFirst() == 1 && prevNodeLanes.getLast() == 3 && toGenerate == 4) {
				prevNodeLanes.remove(1);
			}
			// Edge case 0123->5 and 1234->5
			else if (prevNodeLanes.size() == 4 && prevNodeLanes.getLast() - prevNodeLanes.getFirst() == 3 && toGenerate == 5) {
				if (prevNodeLanes.getFirst() == 0) {
					prevNodeLanes.clear();
					prevNodeLanes.add(3);
				}
				else {
					prevNodeLanes.clear();
					prevNodeLanes.add(1);
				}
			}
			else {
				Collections.shuffle(prevNodeLanes);
				while (prevNodeLanes.size() > nodeDiff) {
					prevNodeLanes.remove(0);
				}
			}

			int nodesWithTwoDests = 0;
			for (int i : GENERATE_ORDER) {
				if (prevPos[i] == null) continue;
				boolean isChosen = prevNodeLanes.remove((Object) i);
				generateMoreDestinations(prevPos[i], type, curr, prevPos, isChosen || (prevNodeLanes.size() == 0 && nodeDiff > nodesWithTwoDests));
				if (prevPos[i].getDestinations().size() == 2) nodesWithTwoDests++;
			}
		}
		else if (nodeDiff == 0) {
			for (int i = 0; i < MAX_LANES; i++) {
				if (prevPos[i] == null) continue;
				generateDestination(prevPos[i], type, curr, prevPos);
			}
		}
		else {
			nodeDiff = -nodeDiff;
			int nodesCombined = 0;
			Node nodeA = null;
			for (int i = 0; i < MAX_LANES; i++) {
				if (prevPos[i] == null) continue;
				if (nodeDiff > nodesCombined) {
					if (nodeA == null) {
						nodeA = prevPos[i];
						continue;
					}
					// Node A is too far to combine with neighbor
					if (i - nodeA.getLane() > 2) {
						generateDestination(nodeA, type, curr, prevPos);
						nodeA = prevPos[i];
						continue;
					}
					generateFewerDestinations(nodeA, prevPos[i], type, curr, prevPos);
					nodeA = null;
					nodesCombined++;
					continue;
				}
				generateDestination(prevPos[i], type, curr, prevPos);
			}
		}
		return curr;
	}
	
	private void generateMoreDestinations(Node from, GenerationType type, Node[] curr, Node[] prev, boolean twoDests) {
		int pos = from.getPosition() + 1, lane = from.getLane();
		// Check available destinations
		LinkedList<Integer> potential = new LinkedList<Integer>();
		for (int i = lane - 1; i <= lane + 1; i++) {
			if (i >= 0 && i < MAX_LANES && curr[i] == null) potential.add(i);
		}
		potential.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				// First priority: Straight lane
				if (i1 == lane) return -1;
				if (i2 == lane) return 1;
				
				// Next priority: edges with no nodes
				if (prev[i1] == null && i1 == 0 || i1 == 4) return -1;
				if (prev[i2] == null && i2 == 0 || i2 == 4) return 1;
				
				// Next priority, lanes with no nodes
				if (prev[i1] == null) return -1;
				if (prev[i2] == null) return 1;
				
				
				// If none of these, random
				return NeoCore.gen.nextBoolean() ? 1 : -1;
			}
		});
		
		// Generate destinations based on priority list
		int destsToGenerate = twoDests ? 2 : 1;
		while (destsToGenerate > from.getDestinations().size() && potential.size() > 0) {
			int newLane = potential.removeFirst();
			curr[newLane] = generateNode(type, pos, newLane, from);
		}
	}
	
	private void generateDestination(Node from, GenerationType type, Node[] curr, Node[] prev) {
		int pos = from.getPosition() + 1, lane = from.getLane();
		// Check available destinations
		LinkedList<Integer> potential = new LinkedList<Integer>();
		for (int i = lane - 1; i <= lane + 1; i++) {
			// Must be within bounds, not have anything in the position,
			// and be an empty lane or the lane it originated from
			if ((i >= 0 && i < MAX_LANES && curr[i] == null && prev[i] == null) || i == lane) potential.add(i);
		}
		
		Collections.shuffle(potential);
		int newLane = potential.removeFirst();
		curr[newLane] = generateNode(type, pos, newLane, from);
	}
	
	private void generateFewerDestinations(Node from1, Node from2, GenerationType type,  Node[] curr, Node[] prev) {
		// from2 must be greater pos than from1
		// If they're 2 apart, destination must be in the middle
		// If 1 apart, it can be in two places
		int newLane = (from2.getLane() - from1.getLane() == 2) ? from2.getLane() - 1 : (NeoCore.gen.nextBoolean() ? from2.getLane() : from1.getLane());
		curr[newLane] = generateNode(type, from1.getPosition() + 1, newLane, from1, from2);
	}

	private Node generateNode(GenerationType type, int pos, int lane, Node... from) {
		NodeType nodeType = type.table.get();
		// Don't allow two of the same node type in a row, unless they're fight or chance nodes
		while (nodeType != NodeType.FIGHT && nodeType != NodeType.CHANCE && !isNodeValid(nodeType, from)) {
			nodeType = type.table.get();
		}
		Node node = new Node(nodeType, pos, lane);
		if (from != null) {
			for (Node n : from) {
				n.addDestination(node);
			}
		}
		return node;
	}
	
	private boolean isNodeValid(NodeType newType, Node[] from) {
		for (Node n : from) {
			if (n.getType() == newType) return false;
		}
		return true;
	}

	public AreaType getType() {
		return type;
	}

	public Node[][] getNodes() {
		return nodes;
	}

	public void saveAll(Statement insert, Statement delete) {
		int saveSlot = s.getSaveSlot();
		UUID host = s.getHost();
		try {
			delete.execute("DELETE FROM neorogue_nodes WHERE host = '" + host + "' AND slot = " + saveSlot + ";");
			for (int pos = 0; pos < MAX_POSITIONS; pos++) {
				for (int lane = 0; lane < MAX_LANES; lane++) {
					Node node = nodes[pos][lane];
					if (node == null) continue;
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_nodes")
							.addString(host.toString()).addValue(saveSlot).addString(node.toString())
							.addValue(node.getPosition()).addValue(node.getLane())
							.addString(node.serializeDestinations()).addString(node.serializeInstanceData());
					insert.addBatch(sql.build());
				}
			}
			insert.executeBatch();
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save nodes for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
	}

	// Only save nodes that need saving (the ones within reach)
	public void saveRelevant(Statement insert, Statement delete) {
		int saveSlot = s.getSaveSlot();
		UUID host = s.getHost();
		try {
			int pos = s.getNode().getPosition() + 1;
			delete.execute("DELETE FROM neorogue_nodes WHERE host = '" + host + "' AND slot = " + saveSlot
					+ " AND position = " + pos + ";");
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node node = nodes[pos][lane];
				if (node == null) continue;
				SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_nodes")
						.addString(host.toString()).addValue(saveSlot).addString(node.toString())
						.addValue(node.getPosition()).addValue(node.getLane()).addString(node.serializeDestinations())
						.addString(node.serializeInstanceData());
				insert.addBatch(sql.build());
			}
			insert.executeBatch();
		} catch (SQLException ex) {
			Bukkit.getLogger()
					.warning("[NeoRogue] Failed to save relevant nodes for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
	}

	public void instantiate() {
		// Create nodes
		org.bukkit.World w = Bukkit.getWorld(WORLD_NAME);
		for (int lane = 0; lane < MAX_LANES; lane++) { // x
			for (int pos = 0; pos < MAX_POSITIONS; pos++) { // z
				Node node = nodes[pos][lane];
				if (node == null) continue;

				Location loc = new Location(w, -(xOff + X_EDGE_PADDING + (lane * NODE_DIST_BETWEEN)), NODE_Y,
						zOff + Z_EDGE_PADDING + (pos * NODE_DIST_BETWEEN));
				loc.getBlock().setType(node.getType().getBlock());
				loc.add(0, 0, -1);
				loc.getBlock().setType(Material.OAK_WALL_SIGN);
				Block b = loc.getBlock();
				Directional dir = (Directional) b.getBlockData();
				dir.setFacing(BlockFace.NORTH);
				b.setBlockData(dir);

				Sign sign = (Sign) b.getState();
				SignSide side = sign.getSide(Side.FRONT);
				sign.getSide(Side.FRONT).line(1, Component.text(node.getType().toString(), null, TextDecoration.BOLD));
				side.setGlowingText(true);
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
	public void update(Node node, NodeSelectInstance inst) {
		// Remove buttons and lecterns from old paths
		int pos = node.getPosition();
		for (int lane = 0; lane < 5; lane++) {
			Node src = nodes[pos][lane];
			if (src == null) continue;
			Location loc = nodeToLocation(src, 1);
			loc.getBlock().setType(Material.AIR);
			loc.add(0, -2, -1);
			loc.getBlock().setType(Material.POLISHED_ANDESITE);
		}

		// Add button to new paths and generate them
		for (Node dest : node.getDestinations()) {
			dest.generateInstance(s, type);

			Location loc = nodeToLocation(dest, 1);
			loc.getBlock().setType(Material.OAK_BUTTON);
			FaceAttachable face = (FaceAttachable) loc.getBlock().getBlockData();
			face.setAttachedFace(AttachedFace.FLOOR);
			loc.getBlock().setBlockData(face);

			// Add holograms to active nodes
			loc.add(0, 2, 0);
			inst.createHologram(loc, dest);

			// Fight nodes
			if (dest.getType() == NodeType.FIGHT || dest.getType() == NodeType.MINIBOSS
					|| dest.getType() == NodeType.BOSS) {
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

	public void tickParticles(Node curr) {
		LinkedList<Player> cache = ParticleUtil.calculateCache(nodeToLocation(curr, 0));
		// Draw red lines for any locations that can immediately be visited
		for (Node dest : curr.getDestinations()) {
			ParticleUtil.drawLineWithCache(cache, red, nodeToLocation(curr, 0.5), nodeToLocation(dest, 0.5), 0.5);
		}

		// Draw black lines for locations past the immediate nodes
		for (int pos = curr.getPosition() + 1; pos < MAX_POSITIONS; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node node = nodes[pos][lane];
				if (node == null) continue;

				for (Node dest : node.getDestinations()) {
					ParticleUtil.drawLine(black, nodeToLocation(node, 0.5), nodeToLocation(dest, 0.5),
							0.5);
				}
			}
		}
	}

	public Location nodeToLocation(Node node, double yOff) {
		org.bukkit.World w = Bukkit.getWorld(WORLD_NAME);
		return new Location(w, -(xOff + X_EDGE_PADDING - 0.5 + (node.getLane() * 4)), NODE_Y + yOff,
				zOff + Z_EDGE_PADDING + 0.5 + (node.getPosition() * 4));
	}

	private void shuffleArray(int[] ar) {
		for (int i = ar.length - 1; i > 0; i--) {
			int index = NeoCore.gen.nextInt(i + 1);
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public enum GenerationType {
		NORMAL(0), SPECIAL(1), INITIAL(2);
		protected DropTable<NodeType> table = new DropTable<NodeType>();

		private GenerationType(int num) {
			switch (num) {
			case 0:
				table.add(NodeType.CHANCE, 30);
				table.add(NodeType.FIGHT, 50);
				table.add(NodeType.MINIBOSS, 2);
				table.add(NodeType.SHOP, 5);
				table.add(NodeType.SHRINE, 5);
				break;
			case 1:
				table.add(NodeType.MINIBOSS, 20);
				table.add(NodeType.SHRINE, 15);
				table.add(NodeType.SHOP, 15);
				table.add(NodeType.CHANCE, 5);
				table.add(NodeType.FIGHT, 5);
				break;
			case 2:
				table.add(NodeType.FIGHT, 50);
				table.add(NodeType.CHANCE, 30);
				break;
			}
		}

	}
}