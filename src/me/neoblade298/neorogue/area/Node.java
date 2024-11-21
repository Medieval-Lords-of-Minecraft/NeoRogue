package me.neoblade298.neorogue.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private Instance inst;

	private int row, lane;
	private NodeType type;
	private List<Node> srcs = new ArrayList<Node>(MAX_DESTS);
	private List<Node> dests = new ArrayList<Node>(MAX_DESTS);

	public Node(int row, int lane, NodeType type) {
		this.row = row;
		this.lane = lane;
		this.type = type;
	}

	public int getRow() {
		return row;
	}
	
	public int getLane() {
		return lane;
	}

	public void setLane(int lane) {
		this.lane = lane;
	}
	
	public List<Node> getSources() {
		return srcs;
	}
	
	public List<Node> getDestinations() {
		return dests;
	}
	
	public void sortDestinations() {
		Collections.sort(dests, (x, y) -> x.lane - y.lane);
	}
	
	public Node addDestination(Node dest) {
		if (!dests.contains(dest))
			dests.add(dest);
		if (!dest.srcs.contains(this))
			dest.addSource(this);
		return this;
	}

	// Just for debugging, to remove start destinations
	public void clear() {
		dests.clear();
		srcs.clear();
	}
	
	public Node addSource(Node src) {
		if (!srcs.contains(src))
			srcs.add(src);
		if (!src.dests.contains(this))
			src.addDestination(this);
		return this;
	}
	
	@Override
	public String toString() {
		return type.name();
	}

	public String serializePosition() {
		return row + "," + lane;
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

	public void setType(NodeType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node n = (Node) obj;
			return lane == n.lane && row == n.row;
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
