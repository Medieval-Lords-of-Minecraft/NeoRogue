package me.neoblade298.neorogue.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.ShopInstance;
import me.neoblade298.neorogue.session.ShrineInstance;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class Node {
	private static int MAX_DESTS = 3;
	private static final Comparator<Node> destinationSorter = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			return Integer.compare(o1.lane, o2.lane);
		}
	};
	
	private ArrayList<Node> dests = new ArrayList<Node>(MAX_DESTS);
	private ArrayList<Node> srcs = new ArrayList<Node>(MAX_DESTS);
	private NodeType type;
	private Instance inst;
	private int pos, lane;
	
	public Node(NodeType type, int pos, int lane) {
		this.type = type;
		this.pos = pos;
		this.lane = lane;
	}
	
	public void addDestination(Node node) {
		dests.add(node);
		node.addSource(this);
	}

	// Basically only used by NodeMapInventory
	public void sortDestinations() {
		Collections.sort(dests, destinationSorter);
	}
	
	private void addSource(Node node) {
		srcs.add(node);
	}
	
	public ArrayList<Node> getDestinations() {
		return dests;
	}
	
	public ArrayList<Node> getSources() {
		return srcs;
	}
	
	public void removeSource(Node node) {
		srcs.remove(node);
	}
	
	public int getPosition() {
		return pos;
	}
	
	public int getLane() {
		return lane;
	}
	
	@Override
	public String toString() {
		return type.name();
	}
	
	public String serializePosition() {
		return pos + "," + lane;
	}

	public void deserializeInstance(Session s, String data) {
		if (!data.isBlank()) {
			inst = FightInstance.deserializeInstanceData(s, s.getParty(), data);
		}
	}

	public String serializeInstanceData() {
		if (inst instanceof FightInstance) {
			return ((FightInstance) inst).serializeInstanceData();
		}
		return "";
	}
	
	public String serializeDestinations() {
		if (dests.isEmpty())
			return "";
		String str = dests.get(0).serializePosition();
		for (int i = 1; i < dests.size(); i++) {
			str += " " + dests.get(i).serializePosition();
		}
		return str;
	}
	
	public NodeType getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node n = (Node) obj;
			return lane == n.lane && pos == n.pos;
		}
		return false;
	}
	
	public Instance generateInstance(Session s, AreaType area) {
		if (inst != null)
			return inst;

		switch (type) {
		case FIGHT:
			inst = new StandardFightInstance(s, s.getParty().keySet(), area, s.getNodesVisited());
			break;
		case SHRINE:
			inst = new ShrineInstance(s);
			break;
		case CHANCE:
			inst = new ChanceInstance(s);
			break;
		case SHOP:
			inst = new ShopInstance(s);
			break;
		case MINIBOSS:
			inst = new MinibossFightInstance(s, s.getParty().keySet(), area);
			break;
		case BOSS:
			inst = new BossFightInstance(s, s.getParty().keySet(), area);
		default:
			break;
		}

		return inst;
	}
	
	public Instance getInstance() {
		return inst;
	}
}
