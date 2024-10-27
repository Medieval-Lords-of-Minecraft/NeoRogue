package me.neoblade298.neorogue.area;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	public static final int MAX_LANES = 5, MAX_POSITIONS = 16, CENTER_LANE = MAX_LANES / 2;
	private static final int X_EDGE_PADDING = 14, Z_EDGE_PADDING = 11, NODE_DIST_BETWEEN = 4;
	private static final double EXTRA_PATH_CHANCE = 0.175;
	private static final double EXTRA_PATH_CHANCE_SQ = EXTRA_PATH_CHANCE * EXTRA_PATH_CHANCE;
	private static final int MAX_CHAIN_LENGTH = 4;
	private static final int MIN_SHOP_DISTANCE = 3; // min # of PATHS not NODES

	private static int[] generate_order;
	
	private static ParticleContainer red = new ParticleContainer(Particle.REDSTONE), black;
	private HashSet<Node> blackTicks = new HashSet<>();
	private static HashMap<Integer, DropTable<Integer>> pathChances = new HashMap<Integer, DropTable<Integer>>();

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
		
		// Load path chances
		DropTable<Integer> paths = new DropTable<Integer>();
		paths.add(2, 9);
		paths.add(3, 66);
		paths.add(4, 25);
		paths.add(5, 0);
		pathChances.put(2, paths);
		
		paths = new DropTable<Integer>();
		paths.add(2, 13);
		paths.add(3, 52);
		paths.add(4, 28);
		paths.add(5, 7);
		pathChances.put(3, paths);
		
		paths = new DropTable<Integer>();
		paths.add(2, 8);
		paths.add(3, 28);
		paths.add(4, 51);
		paths.add(5, 13);
		pathChances.put(4, paths);
		
		paths = new DropTable<Integer>();
		paths.add(2, 0);
		paths.add(3, 22);
		paths.add(4, 54);
		paths.add(5, 24);
		pathChances.put(5, paths);

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
			int pos = rs.getInt("position");
			int lane = rs.getInt("lane");
			Node n = createNode(NodeType.valueOf(rs.getString("type")), pos, lane);
			n.deserializeInstance(s, rs.getString("instanceData"));
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
				if (dest.isBlank())
					continue;
				String[] coords = dest.split(",");
				pos = Integer.parseInt(coords[0]);
				lane = Integer.parseInt(coords[1]);

				node.addDestination(nodes[pos][lane]);
			}
		}
	}

	private void createGenOrder() {
		generate_order = new int[MAX_LANES];
		Set<Integer> used = new HashSet<Integer>();
		
		for (int i = 0; i < MAX_LANES; i++) {
			int gen = NeoRogue.gen.nextInt(MAX_LANES);

			for (int attempts = 0; used.contains(gen)
					|| (attempts < 10 && (used.contains(gen - 1) || used.contains(gen + 1))); attempts++) {
				gen = NeoRogue.gen.nextInt(MAX_LANES);
			}

			used.add(gen);
			generate_order[i] = gen;
		}
	}

	private void generateNodes() {
		do {
			nodes = new Node[MAX_POSITIONS][MAX_LANES];
			createGenOrder();
			tryGenerateNodes();
		} while (!verifyChainLength() || !verifyRequiredMiniboss() || !verifyNoSplitGroups());

		trimShops();
		centralize();

		BossFightInstance bi = (BossFightInstance) nodes[MAX_POSITIONS - 1][CENTER_LANE].generateInstance(s, type); // generate boss
		boss = bi.getBossDisplay();
	}

	private void tryGenerateNodes() {
		// Static nodes
		createNode(NodeType.START, 0, CENTER_LANE);
		createNode(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE - 1);
		createNode(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE);
		createNode(NodeType.SHRINE, MAX_POSITIONS - 2, CENTER_LANE + 1);
		createNode(NodeType.BOSS, MAX_POSITIONS - 1, CENTER_LANE);
		
		// Generate starting positions
		List<Integer> initList = Arrays.asList(0, 1, 2, 3, 4);
		Collections.shuffle(initList);
		int numInit = NeoRogue.gen.nextInt(2, 4); // in range [3,5]
		for (int i = 0; i < numInit; i++) {
			nodes[1][initList.get(i)] = generateNode(GenerationType.INITIAL, 1, initList.get(i), nodes[0][CENTER_LANE]);
		}
		
		// Start generating by position
		for (int pos = 2; pos < MAX_POSITIONS - 2; pos++) {
			GenerationType type;
			
			switch (pos) {
			case 2:
				type = GenerationType.EARLY;
				break;
			case 6:
			case 7:
			case 11:
			case 12:
				type = GenerationType.SPECIAL;
				break;
			case 8:
			case 9:
				type = GenerationType.MIDDLE;
				break;
			case 13:
				type = GenerationType.FINAL;
				break;
			default:
				type = GenerationType.NORMAL;
			}
			
			nodes[pos] = generatePosition(type, nodes[pos - 1]);
		}
		
		// Connect generated nodes to static nodes
		for (int i = 0; i < 5; i++) {
			if (nodes[MAX_POSITIONS - 3][i] == null)
				continue;
			Node node = nodes[MAX_POSITIONS - 3][i];
			if (i == 0 || i == 1) {
				node.addDestination(nodes[MAX_POSITIONS - 2][1]);
			} else if (i == 3 || i == 4) {
				node.addDestination(nodes[MAX_POSITIONS - 2][3]);
			} else {
				node.addDestination(nodes[MAX_POSITIONS - 2][2]);
			}
		}
		
		// Connect end shrines to boss
		for (int i = 1; i <= 3; i++) {
			Node node = nodes[MAX_POSITIONS - 2][i];
			if (node.getSources().isEmpty()) {
				nodes[MAX_POSITIONS - 2][i] = null;
				continue;
			}
			nodes[MAX_POSITIONS - 2][i].addDestination(nodes[MAX_POSITIONS - 1][CENTER_LANE]);
		}
		
		// Second pass for extra paths
		for (int pos = 1; pos < MAX_POSITIONS - 3; pos++) {
			tryAddExtraPaths(pos);
		}
	}
	
	public String getBoss() {
		return boss;
	}

	private boolean verifyRequiredMiniboss() {
		return verifyRequiredMiniboss(nodes[0][CENTER_LANE], nodes[MAX_POSITIONS - 1][CENTER_LANE]);
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

		for (int pos = 1; pos < MAX_POSITIONS - 1; pos++) {
			if (bottomLeft == null) {
				if (nodes[pos][0] != null) {
					bottomLeft = nodes[pos][0];
				} else if (nodes[pos][1] != null) {
					bottomLeft = nodes[pos][1];
				}
			}

			if (bottomRight == null) {
				if (nodes[pos][MAX_LANES - 1] != null) {
					bottomRight = nodes[pos][MAX_LANES - 1];
				} else if (nodes[pos][MAX_LANES - 2] != null) {
					bottomRight = nodes[pos][MAX_LANES - 2];
				}
			}

			if (topLeft == null) {
				if (nodes[MAX_POSITIONS - pos - 2][0] != null) {
					topLeft = nodes[MAX_POSITIONS - pos - 2][0];
				} else if (nodes[MAX_POSITIONS - pos - 2][1] != null) {
					topLeft = nodes[MAX_POSITIONS - pos - 2][1];
				}
			}

			if (topRight == null) {
				if (nodes[MAX_POSITIONS - pos - 2][MAX_LANES - 1] != null) {
					topRight = nodes[MAX_POSITIONS - pos - 2][MAX_LANES - 1];
				} else if (nodes[MAX_POSITIONS - pos - 2][MAX_LANES - 2] != null) {
					topRight = nodes[MAX_POSITIONS - pos - 2][MAX_LANES - 2];
				}
			}
		}
		
		return (bottomLeft == null || hasPath(bottomLeft, bottomLeft.getLane() + 3, MAX_LANES * 2, true))
				&& (bottomRight == null || hasPath(bottomRight, bottomRight.getLane() - 3, MAX_LANES * 2, true))
				&& (topLeft == null || hasPath(topLeft, topLeft.getLane() + 3, MAX_LANES * 2, false))
				&& (topRight == null || hasPath(topRight, topRight.getLane() - 3, MAX_LANES * 2, false));
	}
	
	// returns true if the given node connects to a node in the given lane
	// if forward is true, node is source; else node is destination
	private boolean hasPath(Node node, int lane, int posLimit, boolean forward) {
		if (node.getLane() == lane)
			return true;

		if (posLimit == 0)
			return false;

		for (Node link : forward ? node.getDestinations() : node.getSources()) {
			if (hasPath(link, lane, posLimit - 1, forward))
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
		for (int pos = MAX_POSITIONS - 1; pos >= 0; pos--) {
			List<Integer> checkOrder = Arrays.asList(0, 1, 2, 3, 4);
			Collections.shuffle(checkOrder);
			for (int lane : checkOrder) {
				Node node = nodes[pos][lane];
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
			shop.setType(GenerationType.INITIAL.table.get()); // easy way to avoid breaking path validity
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

	// shifts the whole area left/right so the center of mass is in the center lane if possible
	private void centralize() {
		int[] laneCounts = new int[MAX_LANES];
		
		for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
			for (int lane = 0; lane < MAX_LANES; lane++) {
				if (nodes[pos][lane] != null)
					laneCounts[lane]++;
			}
		}

		double centerOfMass = 0;
		int div = 0;
		for (int lane = 0; lane < MAX_LANES; lane++) {
			centerOfMass += lane * laneCounts[lane];
			div += laneCounts[lane];
		}
		centerOfMass /= div;
		
		final double idealCenterOfMass = (MAX_LANES - 1) / 2.0;
		if (laneCounts[0] == 0 && centerOfMass > idealCenterOfMass + 0.5) {
			for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
				shiftPosLeft(pos);
			}
		} else if (laneCounts[MAX_LANES - 1] == 0 && centerOfMass < idealCenterOfMass - 0.5) {
			for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
				shiftPosRight(pos);
			}
		}

		// also shift indiv. rows to help when the whole area can't shift
		for (int pos = 1; pos < MAX_POSITIONS - 2; pos++) {
			if (nodes[pos][0] == null && nodes[pos][1] == null && nodes[pos][MAX_LANES - 1] != null) {
				boolean canShiftLeft = true;
				for (int lane = 0; lane < MAX_LANES; lane++) {
					if (nodes[pos][lane] == null)
						continue;
					for (Node dest : nodes[pos][lane].getDestinations()) {
						if (dest.getLane() == lane + 1)
							canShiftLeft = false;
					}
					for (Node dest : nodes[pos][lane].getSources()) {
						if (dest.getLane() == lane + 1)
							canShiftLeft = false;
					}
				}
				if (canShiftLeft)
					shiftPosLeft(pos);
			} else if (nodes[pos][MAX_LANES - 1] == null && nodes[pos][MAX_LANES - 2] == null
					&& nodes[pos][0] != null) {
				boolean canShiftRight = true;
				for (int lane = 0; lane < MAX_LANES; lane++) {
					if (nodes[pos][lane] == null)
						continue;
					for (Node dest : nodes[pos][lane].getDestinations()) {
						if (dest.getLane() == lane - 1)
							canShiftRight = false;
					}
					for (Node dest : nodes[pos][lane].getSources()) {
						if (dest.getLane() == lane - 1)
							canShiftRight = false;
					}
				}
				if (canShiftRight)
					shiftPosRight(pos);
			}
		}
	}
	
	private void shiftPosLeft(int pos) {
		for (int lane = 1; lane < MAX_LANES; lane++) {
			nodes[pos][lane - 1] = nodes[pos][lane];
			if (nodes[pos][lane] != null)
				nodes[pos][lane].setLane(lane - 1);
		}
		nodes[pos][MAX_LANES - 1] = null;
	}
	
	private void shiftPosRight(int pos) {
		for (int lane = MAX_LANES - 2; lane >= 0; lane--) {
			nodes[pos][lane + 1] = nodes[pos][lane];
			if (nodes[pos][lane] != null)
				nodes[pos][lane].setLane(lane + 1);
		}
		nodes[pos][0] = null;
	}
	
	// for nodes with 1 or 2 paths out, tries to connect to nodes with 1 path in
	private void tryAddExtraPaths(int currPos) {
		List<Integer> lanes = Arrays.asList(0, 1, 2, 3, 4);
		Collections.shuffle(lanes);
		for (int lane : lanes) {
			Node node = nodes[currPos][lane];
			if (node == null)
				continue;
			
			if (node.getDestinations().size() == 1) { // small chance to add dest if already has 1
				if (NeoRogue.gen.nextDouble() >= EXTRA_PATH_CHANCE)
					continue;
			} else if (node.getDestinations().size() == 2) { // very small chance to add dest if already has 2
				if (NeoRogue.gen.nextDouble() >= EXTRA_PATH_CHANCE_SQ)
					continue;
			} // add no new dests if already has >=3

			if (NeoRogue.gen.nextBoolean()) { // randomly look left or right first
				if (tryAddExtraPathLeft(node))
					continue;
				if (tryAddExtraPathRight(node))
					continue;
				if (tryAddExtraPathStraight(node))
					continue;
			} else {
				if (tryAddExtraPathRight(node))
					continue;
				if (tryAddExtraPathLeft(node))
					continue;
				if (tryAddExtraPathStraight(node))
					continue;
			}
		}
	}
	
	// returns true on success
	private boolean tryAddExtraPathLeft(Node node) {
		if (node.getDestinations().stream().map(Node::getLane).anyMatch(x -> x == node.getLane() - 1))
			return false; // we already have a left path
			
		if (node.getLane() == 0)
			return false;
		
		Node leftNode = nodes[node.getPosition() + 1][node.getLane() - 1];
		if (leftNode == null)
			return false;
		if (leftNode.getSources().size() > 1)
			return false;

		if (!isNodeLinkValid(leftNode.getType(), node))
			return false;
		
		// also need to check new path doesn't cross existing one
		Node sideNode = nodes[node.getPosition()][node.getLane() - 1];
		if (sideNode != null
				&& sideNode.getDestinations().stream().map(Node::getLane).anyMatch(x -> x == node.getLane()))
			return false;
		
		node.addDestination(leftNode);
		return true;
	}
	
	// returns true on success
	private boolean tryAddExtraPathStraight(Node node) {
		if (node.getDestinations().stream().map(Node::getLane).anyMatch(x -> x == node.getLane()))
			return false; // we already have a straight path

		Node straightNode = nodes[node.getPosition() + 1][node.getLane()];
		if (straightNode == null)
			return false;
		if (straightNode.getSources().size() > 1)
			return false;
		
		if (!isNodeLinkValid(straightNode.getType(), node))
			return false;

		node.addDestination(straightNode);
		return true;
	}

	// returns true on success
	private boolean tryAddExtraPathRight(Node node) {
		if (node.getDestinations().stream().map(Node::getLane).anyMatch(x -> x == node.getLane() + 1))
			return false; // we already have a right path

		if (node.getLane() == MAX_LANES - 1)
			return false;
		
		Node rightNode = nodes[node.getPosition() + 1][node.getLane() + 1];
		if (rightNode == null)
			return false;
		if (rightNode.getSources().size() > 1)
			return false;
		
		if (!isNodeLinkValid(rightNode.getType(), node))
			return false;

		// also need to check new path doesn't cross existing one
		Node sideNode = nodes[node.getPosition()][node.getLane() + 1];
		if (sideNode != null
				&& sideNode.getDestinations().stream().map(Node::getLane).anyMatch(x -> x == node.getLane()))
			return false;
		
		node.addDestination(rightNode);
		return true;
	}
	
	private Node[] generatePosition(GenerationType type, Node[] prevPos) {
		Node[] newPos = new Node[MAX_LANES];
		LinkedList<Integer> prevNodeLanes = new LinkedList<Integer>();
		for (Node node : prevPos) {
			if (node != null)
				prevNodeLanes.add(node.getLane());
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
			else if (prevNodeLanes.size() == 3 && prevNodeLanes.getFirst() == 1 && prevNodeLanes.getLast() == 3
					&& toGenerate == 4) {
				prevNodeLanes.remove(1);
			}
			// Edge case 0123->5 and 1234->5
			else if (prevNodeLanes.size() == 4 && prevNodeLanes.getLast() - prevNodeLanes.getFirst() == 3
					&& toGenerate == 5) {
				if (prevNodeLanes.getFirst() == 0) {
					prevNodeLanes.clear();
					prevNodeLanes.add(3);
				} else {
					prevNodeLanes.clear();
					prevNodeLanes.add(1);
				}
			} else {
				Collections.shuffle(prevNodeLanes);
				while (prevNodeLanes.size() > nodeDiff) {
					prevNodeLanes.remove(0);
				}
			}

			int nodesWithTwoDests = 0;
			for (int i : generate_order) {
				if (prevPos[i] == null)
					continue;
				boolean isChosen = prevNodeLanes.remove((Object) i);
				generateMoreDestinations(
						prevPos[i], type, newPos, prevPos,
						isChosen || (prevNodeLanes.size() == 0 && nodeDiff > nodesWithTwoDests)
				);
				if (prevPos[i].getDestinations().size() == 2)
					nodesWithTwoDests++;
			}
		} else if (nodeDiff == 0) {
			if (NeoRogue.gen.nextBoolean()) { // 50/50 between gen'ing left-right vs right-left
				for (int i = 0; i < MAX_LANES; i++) {
					if (prevPos[i] != null)
						generateDestination(prevPos[i], type, newPos, prevPos);
				}
			} else {
				for (int i = MAX_LANES - 1; i >= 0; i--) {
					if (prevPos[i] != null)
						generateDestination(prevPos[i], type, newPos, prevPos);
				}
			}

		} else {
			nodeDiff = -nodeDiff;
			int nodesCombined = 0;
			Node nodeA = null;

			if (NeoRogue.gen.nextBoolean()) { // 50/50 between gen'ing left-right vs right-left
				for (int i = 0; i < MAX_LANES; i++) {
					if (prevPos[i] == null)
						continue;
					
					if (nodeDiff > nodesCombined) {
						if (nodeA == null) {
							nodeA = prevPos[i];
							continue;
						}
						// Node A is too far to combine with neighbor
						if (i - nodeA.getLane() > 2) {
							generateDestination(nodeA, type, newPos, prevPos);
							nodeA = prevPos[i];
							continue;
						}
						generateFewerDestinations(nodeA, prevPos[i], type, newPos, prevPos);
						nodeA = null;
						nodesCombined++;
					} else {
						generateDestination(prevPos[i], type, newPos, prevPos);
					}
				}
			} else {
				for (int i = MAX_LANES - 1; i >= 0; i--) {
					if (prevPos[i] == null)
						continue;

					if (nodeDiff > nodesCombined) {
						if (nodeA == null) {
							nodeA = prevPos[i];
							continue;
						}
						// Node A is too far to combine with neighbor
						if (nodeA.getLane() - i > 2) {
							generateDestination(nodeA, type, newPos, prevPos);
							nodeA = prevPos[i];
							continue;
						}
						generateFewerDestinations(nodeA, prevPos[i], type, newPos, prevPos);
						nodeA = null;
						nodesCombined++;
					} else {
						generateDestination(prevPos[i], type, newPos, prevPos);
					}
				}
			}
		}
		
		cleanLongDiagonals(newPos);
		
		return newPos;
	}
	
	private void cleanLongDiagonals(Node[] pos) {
		for (int lane = 0; lane < MAX_LANES; lane++) {
			Node curr = pos[lane];
			if (curr == null)
				continue;
			for (Node source : curr.getSources()) {
				if (source.getLane() == lane + 2) { // need to shift curr node right 1
					pos[lane + 1] = curr;
					pos[lane] = null;
					curr.setLane(lane + 1);
					break;
				} else if (source.getLane() == lane - 2) { // need to shift curr node left 1
					pos[lane - 1] = curr;
					pos[lane] = null;
					curr.setLane(lane - 1);
					break;
				}
			}
		}
	}
	
	private void generateMoreDestinations(
			Node from, GenerationType type, Node[] newPos, Node[] prevPos, boolean twoDests
	) {
		int pos = from.getPosition() + 1, lane = from.getLane();
		// Check available destinations
		LinkedList<Integer> potential = new LinkedList<Integer>();
		for (int i = lane - 1; i <= lane + 1; i++) {
			if (i >= 0 && i < MAX_LANES && newPos[i] == null)
				potential.add(i);
		}
		potential.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				// First priority: Straight lane
				if (i1 == lane)
					return -1;
				if (i2 == lane)
					return 1;
				
				// Next priority: Edges with no nodes
				if (prevPos[i1] == null && (i1 == 0 || i1 == MAX_LANES - 1))
					return -1;
				if (prevPos[i2] == null && (i2 == 0 || i2 == MAX_LANES - 1))
					return 1;
				
				// Next priority: Lanes with no nodes
				if (prevPos[i1] == null)
					return -1;
				if (prevPos[i2] == null)
					return 1;

				// If none of these: Random
				return NeoRogue.gen.nextBoolean() ? 1 : -1;
			}
		});
		
		// Generate destinations based on priority list
		int destsToGenerate = twoDests ? 2 : 1;
		while (destsToGenerate > from.getDestinations().size() && potential.size() > 0) {
			int newLane = potential.removeFirst();
			newPos[newLane] = generateNode(type, pos, newLane, from);
		}
	}
	
	private void generateDestination(Node from, GenerationType type, Node[] newPos, Node[] prevPos) {
		int pos = from.getPosition() + 1, lane = from.getLane();
		// Check available destinations
		LinkedList<Integer> potential = new LinkedList<Integer>();
		for (int i = lane - 1; i <= lane + 1; i++) {
			// Must be within bounds, not have anything in the position,
			// and be an empty lane or the lane it originated from
			if ((i >= 0 && i < MAX_LANES && newPos[i] == null && prevPos[i] == null) || i == lane)
				potential.add(i);
		}
		
		Collections.shuffle(potential);
		int newLane = potential.removeFirst();
		newPos[newLane] = generateNode(type, pos, newLane, from);
	}
	
	private void generateFewerDestinations(Node from1, Node from2, GenerationType type, Node[] newPos, Node[] prevPos) {
		// from2 must be greater pos than from1
		// If they're 2 apart, destination must be in the middle
		// If 1 apart, it can be in two places
		int newLane;
		if (from2.getLane() - from1.getLane() == 2)
			newLane = from2.getLane() - 1;
		else
			newLane = NeoRogue.gen.nextBoolean() ? from2.getLane() : from1.getLane();
		
		newPos[newLane] = generateNode(type, from1.getPosition() + 1, newLane, from1, from2);
	}

	private Node generateNode(GenerationType type, int pos, int lane, Node... from) {
		NodeType nodeType;
		do {
			nodeType = type.table.get();
		} while (!isNodeLinkValid(nodeType, from));

		Node node = new Node(nodeType, pos, lane);
		for (Node n : from) {
			n.addDestination(node);
		}
		return node;
	}
	
	private boolean isNodeLinkValid(NodeType newType, Node... from) {
		if (newType == NodeType.FIGHT)
			return true;
		if (newType == NodeType.CHANCE)
			return true;
		
		for (Node n : from) {
			if (n.getType() == newType)
				return false;
		}
		return true;
	}

	private Node createNode(NodeType type, int pos, int lane) {
		Node n = new Node(type, pos, lane);
		nodes[pos][lane] = n;
		return n;
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
					if (node == null)
						continue;
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
			delete.execute(
					"DELETE FROM neorogue_nodes WHERE host = '" + host + "' AND slot = " + saveSlot + " AND position = "
							+ pos + ";"
			);
			for (int lane = 0; lane < MAX_LANES; lane++) {
				Node node = nodes[pos][lane];
				if (node == null)
					continue;
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
				if (node == null)
					continue;

				Location loc = new Location(
						w, -(xOff + X_EDGE_PADDING + (lane * NODE_DIST_BETWEEN)), NODE_Y,
						zOff + Z_EDGE_PADDING + (pos * NODE_DIST_BETWEEN)
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
				zOff + Z_EDGE_PADDING + 0.5 + (node.getPosition() * 4)
		);
	}

	public enum GenerationType {
		NORMAL(0), SPECIAL(1), INITIAL(2), FINAL(3), EARLY(4), MIDDLE(5);

		protected DropTable<NodeType> table = new DropTable<NodeType>();

		private GenerationType(int num) {
			switch (num) {
			case 0:
				table.add(NodeType.FIGHT, 60);
				table.add(NodeType.CHANCE, 27);
				table.add(NodeType.SHOP, 2);
				table.add(NodeType.MINIBOSS, 5);
				table.add(NodeType.SHRINE, 6);
				break;
			case 1:
				table.add(NodeType.FIGHT, 10);
				table.add(NodeType.CHANCE, 10);
				table.add(NodeType.SHOP, 8);
				table.add(NodeType.MINIBOSS, 40);
				table.add(NodeType.SHRINE, 32);
				break;
			case 2:
				table.add(NodeType.FIGHT, 69);
				table.add(NodeType.CHANCE, 31);
				break;
			case 3:
				table.add(NodeType.FIGHT, 64);
				table.add(NodeType.CHANCE, 31);
				table.add(NodeType.SHOP, 5);
				break;
			case 4:
				table.add(NodeType.FIGHT, 57);
				table.add(NodeType.CHANCE, 31);
				table.add(NodeType.MINIBOSS, 2);
				table.add(NodeType.SHRINE, 10);
				break;
			case 5:
				table.add(NodeType.FIGHT, 24);
				table.add(NodeType.CHANCE, 14);
				table.add(NodeType.SHOP, 15);
				table.add(NodeType.MINIBOSS, 5);
				table.add(NodeType.SHRINE, 42);
				break;
			}
		}

	}
}
