package me.neoblade298.neorogue.area;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.Effect;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
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
	public static final int LANE_COUNT = 5, ROW_COUNT = 16, CENTER_LANE = LANE_COUNT / 2;
	private static final int X_EDGE_PADDING = 14, Z_EDGE_PADDING = 11, NODE_DIST_BETWEEN = 4;
	private static final int MAX_CHAIN_LENGTH = 4;
	private static final int MIN_SHOP_DISTANCE = 3; // min # of PATHS not NODES
	
	private static ParticleContainer red = new ParticleContainer(Particle.DUST), black;
	private HashSet<Node> blackTicks = new HashSet<>();

	private AreaType type;
	private Node[][] nodes;
	private Session s;
	private String boss;

	public static World world;
	public static final String WORLD_NAME = "Dev";
	private static final int NODE_Y = 64;

	// Offsets
	private int xOff, zOff;

	private static boolean initialized = false;

	public static void initialize() {
		world = BukkitAdapter.adapt(Bukkit.getWorld(WORLD_NAME));

		// Load particles
		red.count(3).spread(0.1, 0.1).forceVisible(Audience.ALL).dustOptions(new DustOptions(Color.RED, 1F));
		black = red.clone().dustOptions(new DustOptions(Color.BLACK, 1F));

		initialized = true;
	}

	public Area(AreaType type, int xOff, int zOff, Session s) {
		if (!initialized)
			initialize();
		
		this.type = type;
		this.xOff = xOff;
		this.zOff = zOff + Session.AREA_Z;
		this.s = s;
		
		generateNodes();

		// Should only save all nodes at first, on auto-save only save nodes within reach (for instance data)
		new BukkitRunnable() {
			@Override
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
			int row = rs.getInt("position");
			int lane = rs.getInt("lane");
			Node n = createNode(row, lane, NodeType.valueOf(rs.getString("type")));
			n.deserializeInstance(s, rs.getString("instanceData"));
		}

		// Next load the node destinations now that they're populated
		// have to redo the statement since resultsets are type forward only
		rs = stmt.executeQuery("SELECT * FROM neorogue_nodes WHERE host = '" + uuid + "' AND slot = " + saveSlot + ";");
		while (rs.next()) {
			int row = rs.getInt("position");
			int lane = rs.getInt("lane");
			Node node = nodes[row][lane];

			String[] dests = rs.getString("destinations").split(" ");
			for (String dest : dests) {
				if (dest.isBlank())
					continue;
				String[] coords = dest.split(",");
				row = Integer.parseInt(coords[0]);
				lane = Integer.parseInt(coords[1]);

				node.addDestination(nodes[row][lane]);
			}
		}
	}

	private void generateNodes() {
		do {
			tryGenerateNodes();
		} while (!verifyShopCount(2) || !verifyChainLength() || !verifyRequiredMiniboss() || !verifyNoSplitGroups());

		trimShops();
		
		BossFightInstance bi = (BossFightInstance) nodes[ROW_COUNT - 1][CENTER_LANE].generateInstance(s, type); // generate boss
		boss = bi.getBossDisplay();
	}

	private void tryGenerateNodes() {
		nodes = new Node[ROW_COUNT][LANE_COUNT];
		
		// Static nodes
		Node startNode = createNode(0, CENTER_LANE, NodeType.START);
		Node endShrine = createNode(ROW_COUNT - 2, CENTER_LANE, NodeType.SHRINE);
		Node bossNode = createNode(ROW_COUNT - 1, CENTER_LANE, NodeType.BOSS);
		endShrine.addDestination(bossNode);
		
		// Entry nodes
		for (int lane = 1; lane <= LANE_COUNT - 2; lane++) {
			createNode(1, lane, getRandomNodeType(1)).addSource(startNode);
		}
		
		// Normal nodes
		for (int row = 2; row <= ROW_COUNT - 4; row++) {
			generateNormalRow(row);
			centralizeRow(row);
		}
		
		// Exit nodes
		for (int lane = 1; lane <= LANE_COUNT - 2; lane++) {
			createNode(ROW_COUNT - 3, lane, getRandomNodeType(1)).addDestination(endShrine);
		}

		// Connect to exit nodes
		// NOTE: Ignores LANE_COUNT
		if (nodes[ROW_COUNT - 4][0] != null)
			nodes[ROW_COUNT - 4][0].addDestination(nodes[ROW_COUNT - 3][1]);
		if (nodes[ROW_COUNT - 4][2] != null)
			nodes[ROW_COUNT - 4][2].addDestination(nodes[ROW_COUNT - 3][2]);
		if (nodes[ROW_COUNT - 4][4] != null)
			nodes[ROW_COUNT - 4][4].addDestination(nodes[ROW_COUNT - 3][3]);
		Node n = nodes[ROW_COUNT - 4][1];
		if (n != null) {
			if (NeoCore.gen.nextBoolean())
				n.addDestination(nodes[ROW_COUNT - 3][1]);
			else
				n.addDestination(nodes[ROW_COUNT - 3][2]);
		}
		n = nodes[ROW_COUNT - 4][3];
		if (n != null) {
			if (NeoCore.gen.nextBoolean())
				n.addDestination(nodes[ROW_COUNT - 3][2]);
			else
				n.addDestination(nodes[ROW_COUNT - 3][3]);
		}
		
		// Delete unconnected entry/exit nodes
		for (int lane = 1; lane <= LANE_COUNT - 2; lane++) {
			if (nodes[1][lane].getDestinations().size() == 0)
				nodes[1][lane] = null;
			if (nodes[ROW_COUNT - 3][lane].getSources().size() == 0)
				nodes[ROW_COUNT - 3][lane] = null;
		}
	}
	
	public String getBoss() {
		return boss;
	}
	
	// ensures at least cnt many shops exist
	private boolean verifyShopCount(int cnt) {
		int found = 0;
		for (int row = 0; row < ROW_COUNT; row++) {
			for (int lane = 0; lane < LANE_COUNT; lane++) {
				if (nodes[row][lane] != null && nodes[row][lane].getType() == NodeType.SHOP) {
					found++;
					if (found >= cnt)
						return true;
				}
			}
		}
		return false;
	}

	private boolean verifyRequiredMiniboss() {
		return verifyRequiredMiniboss(nodes[0][CENTER_LANE], nodes[ROW_COUNT - 1][CENTER_LANE]);
	}

	// returns true if all paths from start to end contain at least one miniboss node
	// assumes at least one path from start to end exists
	private boolean verifyRequiredMiniboss(Node start, Node end) {
		if (start.getType() == NodeType.MINIBOSS)
			return true;
		if (start.equals(end))
			return false;

		for (Node dest : start.getDestinations()) {
			if (!verifyRequiredMiniboss(dest, end))
				return false;
		}
		// possible long-term todo: cache nodes so they aren't checked multiple times

		return true;
	}

	// returns true if the first node in the rightmost lane has a path to a node in the leftmost lane,
	//           and if the first node in the lefttmost lane has a path to a node in the rightmost lane,
	//           and if a node in the rightmost lane has a path to the last node in the leftmost lane,
	//           and if a node in the lefttmost lane has a path to the last node in the rightmost lane
	private boolean verifyNoSplitGroups() {
		Node bottomLeft = null, bottomRight = null, topLeft = null, topRight = null;

		for (int row = 1; row < ROW_COUNT - 1; row++) {
			if (bottomLeft == null) {
				if (nodes[row][0] != null) {
					bottomLeft = nodes[row][0];
				} else if (nodes[row][1] != null) {
					bottomLeft = nodes[row][1];
				}
			}

			if (bottomRight == null) {
				if (nodes[row][LANE_COUNT - 1] != null) {
					bottomRight = nodes[row][LANE_COUNT - 1];
				} else if (nodes[row][LANE_COUNT - 2] != null) {
					bottomRight = nodes[row][LANE_COUNT - 2];
				}
			}

			if (topLeft == null) {
				if (nodes[ROW_COUNT - row - 2][0] != null) {
					topLeft = nodes[ROW_COUNT - row - 2][0];
				} else if (nodes[ROW_COUNT - row - 2][1] != null) {
					topLeft = nodes[ROW_COUNT - row - 2][1];
				}
			}

			if (topRight == null) {
				if (nodes[ROW_COUNT - row - 2][LANE_COUNT - 1] != null) {
					topRight = nodes[ROW_COUNT - row - 2][LANE_COUNT - 1];
				} else if (nodes[ROW_COUNT - row - 2][LANE_COUNT - 2] != null) {
					topRight = nodes[ROW_COUNT - row - 2][LANE_COUNT - 2];
				}
			}
		}
		
		return (bottomLeft == null || hasPath(bottomLeft, bottomLeft.getLane() + 3, LANE_COUNT * 2, true))
				&& (bottomRight == null || hasPath(bottomRight, bottomRight.getLane() - 3, LANE_COUNT * 2, true))
				&& (topLeft == null || hasPath(topLeft, topLeft.getLane() + 3, LANE_COUNT * 2, false))
				&& (topRight == null || hasPath(topRight, topRight.getLane() - 3, LANE_COUNT * 2, false));
	}
	
	// returns true if the given node connects to a node in the given lane
	// if forward is true, node is source; else node is destination
	private boolean hasPath(Node node, int lane, int rowLimit, boolean forward) {
		if (node.getLane() == lane)
			return true;

		if (rowLimit == 0)
			return false;

		for (Node link : forward ? node.getDestinations() : node.getSources()) {
			if (hasPath(link, lane, rowLimit - 1, forward))
				return true;
		}

		return false;
	}
	
	private boolean verifyChainLength() {
		Map<Node, Integer> nodeChainLengths = new HashMap<>();
		calcChainLength(nodeChainLengths, nodes[0][CENTER_LANE]);
		for (int len : nodeChainLengths.values()) { // cringe
			if (len > MAX_CHAIN_LENGTH)
				return false;
		}
		return true;
	}

	private int calcChainLength(Map<Node, Integer> allLengths, Node currNode) {
		if (allLengths.containsKey(currNode))
			return allLengths.get(currNode);
		
		int myLength;
		if (currNode.getDestinations().size() == 1) {
			myLength = 1 + calcChainLength(allLengths, currNode.getDestinations().get(0));
		} else {
			myLength = 0;
			for (Node dest : currNode.getDestinations()) {
				calcChainLength(allLengths, dest);
			}
		}

		allLengths.put(currNode, myLength);
		return myLength;
	}

	// deletes/transforms shops whose only path(s) lead to another shop too soon
	private void trimShops() {
		for (int row = ROW_COUNT - 1; row >= 0; row--) {
			List<Integer> checkOrder = Arrays.asList(0, 1, 2, 3, 4);
			Collections.shuffle(checkOrder);
			for (int lane : checkOrder) {
				Node node = nodes[row][lane];
				if (node != null && node.getType() == NodeType.SHOP)
					tryTrimShop(node);
			}
		}
	}
	
	private void tryTrimShop(Node shop) {
		boolean canAvoid = false;
		for (Node dest : shop.getDestinations()) {
			if (canAvoidShopPath(dest, MIN_SHOP_DISTANCE - 1)) {
				canAvoid = true;
				break;
			}
		}

		if (!canAvoid) {
			shop.setType(GenerationType.ENTRY.table.get()); // easy way to avoid breaking path validity
		}
	}
	
	private boolean canAvoidShopPath(Node start, int remainingLookahead) {
		if (remainingLookahead == 0)
			return true;
		if (start.getType() == NodeType.SHOP)
			return false;
		
		for (Node dest : start.getDestinations()) {
			if (canAvoidShopPath(dest, remainingLookahead - 1))
				return true;
		}
		return false;
	}
	
	private void generateNormalRow(int row) {
		int prevCnt = 0;
		for (int lane = 0; lane < LANE_COUNT; lane++) {
			if (nodes[row - 1][lane] != null)
				prevCnt++;
		}

		double bonusNodeProb;
		switch (prevCnt) {
		case 1:
			bonusNodeProb = 1;
			break;
		case 2:
			bonusNodeProb = 0.65;
			break;
		case 3:
			bonusNodeProb = 0.4;
			break;
		case 4:
			bonusNodeProb = 0.1;
			break;
		case 5:
			bonusNodeProb = 0.025;
			break;
		default:
			bonusNodeProb = 0;
			break;
		}

		for (int lane = 0; lane < LANE_COUNT; lane++) {
			if (nodes[row - 1][lane] != null) {
				int minLane = Math.max(0, lane - 1);
				int maxLane = Math.min(lane + 1, LANE_COUNT - 1);

				Node n;
				final int laneCopy = lane; // thanks for making me do this java
				if (lane > 0 && (n = nodes[row - 1][lane - 1]) != null) {
					if (n.getDestinations().stream().anyMatch(x -> x.getLane() == laneCopy))
						minLane = lane;
				}
				if (lane < LANE_COUNT - 1 && (n = nodes[row - 1][lane + 1]) != null) {
					if (n.getDestinations().stream().anyMatch(x -> x.getLane() == laneCopy))
						maxLane = lane;
				}

				int newLane = NeoCore.gen.nextInt(minLane, maxLane + 1);
				createNode(row, newLane, nodes[row - 1][lane]);

				if (NeoCore.gen.nextDouble() < bonusNodeProb) {
					for (int i = 0; i < 10; i++) { // 10 attempts to avoid infinite loop
						int bonusLane = NeoCore.gen.nextInt(minLane, maxLane + 1);
						if (bonusLane != newLane) {
							createNode(row, bonusLane, nodes[row - 1][lane]);
							break;
						}
					}
				}
			}
		}
	}

	private void centralizeRow(int row) {
		// try move left
		while (canShiftOver(row, true) && getCenterOfMass(row) > 2.5 && nodes[row][0] == null) {
			for (int lane = 0; lane < LANE_COUNT - 1; lane++) {
				nodes[row][lane] = nodes[row][lane + 1];
				if (nodes[row][lane] != null)
					nodes[row][lane].setLane(lane);
			}
			nodes[row][LANE_COUNT - 1] = null;
		}
		// try move right
		while (canShiftOver(row, false) && getCenterOfMass(row) < 1.5 && nodes[row][LANE_COUNT - 1] == null) {
			for (int lane = LANE_COUNT - 1; lane >= 1; lane--) {
				nodes[row][lane] = nodes[row][lane - 1];
				if (nodes[row][lane] != null)
					nodes[row][lane].setLane(lane);
			}
			nodes[row][0] = null;
		}
	}

	private boolean canShiftOver(int row, boolean left) {
		for (int lane = 0; lane < LANE_COUNT; lane++) {
			if (nodes[row][lane] != null) {
				for (Node src : nodes[row][lane].getSources()) {
					if ((left && src.getLane() > lane) || (!left && src.getLane() < lane))
						return false;
				}
			}
		}
		return true;
	}

	private double getCenterOfMass(int row) {
		double sum = 0;
		int cnt = 0;
		for (int lane = 0; lane < LANE_COUNT; lane++) {
			if (nodes[row][lane] != null) {
				sum += lane;
				cnt++;
			}
		}
		return sum / cnt;
	}

	private Node createNode(int row, int lane, NodeType type) {
		if (nodes[row][lane] != null)
			return nodes[row][lane];
		
		Node node = new Node(row, lane, type);
		nodes[row][lane] = node;
		return node;
	}
	
	private Node createNode(int row, int lane, Node... sources) {
		if (nodes[row][lane] != null) {
			Node existingNode = nodes[row][lane];
			for (Node src : sources)
				existingNode.addSource(src);

			while (!isNodeTypeValid(existingNode.getType(), existingNode.getSources().toArray(new Node[0]))) { // gross, again thank you java
				existingNode.setType(getRandomNodeType(row));
			}

			return existingNode;
		}
		
		NodeType type;
		do {
			type = getRandomNodeType(row);
		} while (!isNodeTypeValid(type, sources));
		
		Node node = new Node(row, lane, type);
		nodes[row][lane] = node;
		
		for (Node src : sources)
			node.addSource(src);
		
		return node;
	}
	
	private boolean isNodeTypeValid(NodeType destType, Node... sources) {
		if (destType == NodeType.FIGHT)
			return true;
		if (destType == NodeType.CHANCE)
			return true;
		
		for (Node src : sources)
			if (destType == src.getType())
				return false;

		return true;
	}

	// NOTE: ignores ROW_COUNT
	private NodeType getRandomNodeType(int row) {
		switch (row) {
		case 0:
			return NodeType.START;
		case 1:
			return GenerationType.ENTRY.table.get();
		case 2:
		case 3:
		case 4:
		case 5:
			return GenerationType.EARLY.table.get();
		case 6:
		case 7:
		case 8:
			return GenerationType.MIDDLE.table.get();
		case 9:
		case 10:
		case 11:
		case 12:
			return GenerationType.LATE.table.get();
		case 13:
			return GenerationType.EXIT.table.get();
		case 14:
			return NodeType.SHRINE;
		case 15:
			return NodeType.BOSS;
		default:
			return NodeType.START; // should never happen, so this makes it obvious if it does
		}
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
			for (int row = 0; row < ROW_COUNT; row++) {
				for (int lane = 0; lane < LANE_COUNT; lane++) {
					Node node = nodes[row][lane];
					if (node == null)
						continue;
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_nodes")
							.addString(host.toString()).addValue(saveSlot).addString(node.toString())
							.addValue(node.getRow()).addValue(node.getLane()).addString(node.serializeDestinations())
							.addString(node.serializeInstanceData());
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
			int row = s.getNode().getRow() + 1;
			delete.execute(
					"DELETE FROM neorogue_nodes WHERE host = '" + host + "' AND slot = " + saveSlot + " AND position = "
							+ row + ";"
			);
			for (int lane = 0; lane < LANE_COUNT; lane++) {
				Node node = nodes[row][lane];
				if (node == null)
					continue;
				SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_nodes")
						.addString(host.toString()).addValue(saveSlot).addString(node.toString())
						.addValue(node.getRow()).addValue(node.getLane()).addString(node.serializeDestinations())
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
		for (int lane = 0; lane < LANE_COUNT; lane++) { // x
			for (int row = 0; row < ROW_COUNT; row++) { // z
				Node node = nodes[row][lane];
				if (node == null)
					continue;

				Location loc = new Location(
						w, -(xOff + X_EDGE_PADDING + (lane * NODE_DIST_BETWEEN)), NODE_Y,
						zOff + Z_EDGE_PADDING + (row * NODE_DIST_BETWEEN)
				);
				loc.getBlock().setType(node.getType().getBlock());
				loc.add(0, 0, -1);
				loc.getBlock().setType(Material.OAK_WALL_SIGN);
				Block b = loc.getBlock();
				Directional dir = (Directional) b.getBlockData();
				dir.setFacing(BlockFace.NORTH);
				b.setBlockData(dir);

				Sign sign = (Sign) b.getState();
				sign.setWaxed(true);
				SignSide side = sign.getSide(Side.FRONT);
				sign.getSide(Side.FRONT).line(1, Component.text(node.getType().toString(), null, TextDecoration.BOLD));
				side.setGlowingText(true);
				sign.update();
			}
		}
	}

	public Node getNodeFromLocation(Location loc) {
		int row = loc.getBlockZ(), lane = loc.getBlockX();
		lane += xOff + X_EDGE_PADDING;
		lane /= NODE_DIST_BETWEEN;
		row -= zOff + Z_EDGE_PADDING;
		row /= NODE_DIST_BETWEEN;
		return nodes[row][-lane];
	}

	// Called whenever a player advances to a new node
	public void update(Node node, NodeSelectInstance inst) {
		// Remove buttons and lecterns from old paths
		int row = node.getRow();
		for (int lane = 0; lane < 5; lane++) {
			Node src = nodes[row][lane];
			if (src == null)
				continue;
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

		blackTicks.clear();
	}
	
	public void tickParticles(Node curr) {
		LinkedList<Player> cache = Effect.calculateCache(nodeToLocation(curr, 0));
		// Draw red lines for any locations that can immediately be visited
		for (Node dest : curr.getDestinations()) {
			ParticleUtil.drawLineWithCache(cache, red, nodeToLocation(curr, 0.5), nodeToLocation(dest, 0.5), 0.5);
		}
		
		// Draw black lines for locations past the immediate nodes
		if (blackTicks.size() == 0) {
			for (Node dest : curr.getDestinations()) {
				addToBlackTicks(dest);
			}
		}
		
		for (Node tick : blackTicks) {
			cache = Effect.calculateCache(nodeToLocation(tick, 0));
			for (Node dest : tick.getDestinations()) {
				ParticleUtil.drawLineWithCache(cache, black, nodeToLocation(tick, 0.5), nodeToLocation(dest, 0.5), 0.5);
			}
		}
	}
	
	private void addToBlackTicks(Node node) {
		if (blackTicks.contains(node))
			return;

		blackTicks.add(node);
		for (Node dest : node.getDestinations()) {
			addToBlackTicks(dest);
		}
	}
	
	public Location nodeToLocation(Node node, double yOff) {
		org.bukkit.World w = Bukkit.getWorld(WORLD_NAME);
		return new Location(
				w, -(xOff + X_EDGE_PADDING - 0.5 + (node.getLane() * 4)), NODE_Y + yOff,
				zOff + Z_EDGE_PADDING + 0.5 + (node.getRow() * 4)
		);
	}

	public enum GenerationType {
		EARLY(0), MIDDLE(1), LATE(2), ENTRY(3), EXIT(4);

		protected DropTable<NodeType> table = new DropTable<NodeType>();

		private GenerationType(int num) {
			switch (num) {
			case 0:
				table.add(NodeType.FIGHT, 55);
				table.add(NodeType.CHANCE, 30);
				table.add(NodeType.SHOP, 4);
				table.add(NodeType.MINIBOSS, 10);
				table.add(NodeType.SHRINE, 1);
				break;
			case 1:
				table.add(NodeType.FIGHT, 2);
				table.add(NodeType.CHANCE, 5);
				table.add(NodeType.SHOP, 8);
				table.add(NodeType.MINIBOSS, 70);
				table.add(NodeType.SHRINE, 15);
				break;
			case 2:
				table.add(NodeType.FIGHT, 30);
				table.add(NodeType.CHANCE, 10);
				table.add(NodeType.SHOP, 20);
				table.add(NodeType.MINIBOSS, 15);
				table.add(NodeType.SHRINE, 25);
				break;
			case 3:
				table.add(NodeType.FIGHT, 60);
				table.add(NodeType.CHANCE, 40);
				break;
			case 4:
				table.add(NodeType.FIGHT, 50);
				table.add(NodeType.CHANCE, 40);
				table.add(NodeType.SHOP, 10);
				break;
			}
		}

	}
}
