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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.NeoRogue;

public class Area {
	private AreaType type;
	private Node[][] nodes = new Node[MAX_POSITIONS][MAX_LANES];
	private static World world;

	private static final int MIN_MINIBOSSES = 2;
	private static final int MIN_SHOPS = 3;
	private static final int MAX_LANES = 5;
	private static final int MAX_POSITIONS = 12;
	private static final int CENTER_LANE = MAX_LANES / 2;

	// Constants for path generation
	private static final double STRAIGHT_PATH_CHANCE = 0.7;
	private static final double DOUBLE_PATH_CHANCE = 0.4;
	
	// schematics
	private static String NODE_SELECT = "nodeselect.schem";

	// Deserialize
	public Area(AreaType type, UUID uuid, int saveSlot, Statement stmt) throws SQLException {
		this.type = type;

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

	// Generate new
	public Area(AreaType type) {
		this.type = type;

		// Static nodes
		nodes[0][CENTER_LANE] = new Node(NodeType.START, 0, CENTER_LANE);
		nodes[1][CENTER_LANE - 2] = generateNode(true, 1, CENTER_LANE - 2);
		nodes[1][CENTER_LANE - 1] = generateNode(true, 1, CENTER_LANE - 1);
		nodes[1][CENTER_LANE] = generateNode(true, 1, CENTER_LANE);
		nodes[1][CENTER_LANE + 1] = generateNode(true, 1, CENTER_LANE + 1);
		nodes[1][CENTER_LANE + 2] = generateNode(true, 1, CENTER_LANE + 2);
		nodes[MAX_POSITIONS - 2][CENTER_LANE - 1] = new Node(NodeType.REST, MAX_POSITIONS - 2, CENTER_LANE - 1);
		nodes[MAX_POSITIONS - 2][CENTER_LANE] = new Node(NodeType.REST, MAX_POSITIONS - 2, CENTER_LANE);
		nodes[MAX_POSITIONS - 2][CENTER_LANE + 1] = new Node(NodeType.REST, MAX_POSITIONS - 2, CENTER_LANE + 1);
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
				if (nodes[pos][lane] == null) continue;
				if (nodes[pos][lane].getSources().size() == 0) {
					nodes[pos][lane] = null;
				}
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
		 * Base chances: Fight 25%, Rest 5%, Miniboss 10%, Shop 5%, Event 25%, Nothing
		 * 30% Minimum Minibosses 2, Shops 3,
		 */
		double rand = NeoCore.gen.nextDouble();

		rand -= 0.25;
		if (rand < 0) return new Node(NodeType.FIGHT, pos, lane);
		rand -= 0.05;
		if (rand < 0) return new Node(NodeType.REST, pos, lane);
		rand -= 0.1;
		if (rand < 0) return new Node(NodeType.MINIBOSS, pos, lane);
		rand -= 0.05;
		if (rand < 0) return new Node(NodeType.SHOP, pos, lane);
		rand -= 0.25;
		if (rand < 0) return new Node(NodeType.EVENT, pos, lane);

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

	public void print() {
		for (int lane = 0; lane < MAX_LANES; lane++) {
			for (int pos = 0; pos < MAX_POSITIONS; pos++) {
				Node node = nodes[pos][lane];
				if (node == null) {
					System.out.print("N/A ");
				}
				else {
					System.out.print(nodes[pos][lane] + " ");
				}
			}
			System.out.println();
			System.out.println();
		}
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

	public void generate(int xOff, int zOff) {
		// Paste the schematic
		Clipboard clipboard = null;

		File file = new File(NeoRogue.SCHEMATIC_FOLDER, NODE_SELECT);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (world == null) Bukkit.getWorld("Artoria");
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
		    Operation operation = new ClipboardHolder(clipboard)
		            .createPaste(editSession)
		            .to(BlockVector3.at(xOff, 64, zOff))
		            .build();
		    try {
				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		// Create area within schematic
		org.bukkit.World w = Bukkit.getWorld("Artoria");

		for (int lane = 0; lane < MAX_LANES; lane++) {
			for (int pos = 0; pos < MAX_POSITIONS; pos++) {
				Location loc = new Location(w, xOff + 6 + (pos * 3), 65, zOff + 6 + (lane * 3));
				loc.getBlock().setType(nodes[pos][lane].getType().getBlock());
				
				loc.add(0, 1, 0);
				loc.getBlock().setType(Material.OAK_BUTTON);
				Directional dir = (Directional) loc.getBlock().getBlockData();
				dir.setFacing(BlockFace.UP);
			}
		}
	}
}
