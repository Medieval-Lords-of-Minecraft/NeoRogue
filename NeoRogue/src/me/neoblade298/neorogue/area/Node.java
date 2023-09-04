package me.neoblade298.neorogue.area;

import java.util.ArrayList;

import me.neoblade298.neorogue.session.*;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.fights.BossFightInstance;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.MinibossFightInstance;
import me.neoblade298.neorogue.session.fights.StandardFightInstance;

public class Node {
	private static int MAX_DESTS = 3;

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

	public void addSource(Node node) {
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
			inst = FightInstance.deserializeInstanceData(s, data);
		}
	}
	
	public String serializeInstanceData() {
		if (inst instanceof FightInstance) {
			return ((FightInstance) inst).serializeInstanceData();
		}
		return "";
	}

	public String serializeDestinations() {
		if (dests.isEmpty()) return "";
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

	public void generateInstance(Session s, AreaType area) {
		switch (type) {
		case FIGHT:
			inst = new StandardFightInstance(area, s.getNodesVisited());
			break;
		case CAMPFIRE:
			inst = new CampfireInstance();
			break;
		case CHANCE:
			inst = new ChanceInstance();
			break;
		case SHOP:
			inst = new ShopInstance();
			break;
		case MINIBOSS:
			inst = new MinibossFightInstance(area);
			break;
		case BOSS:
			inst = new BossFightInstance(area);
		default:
			break;
		}
	}

	public Instance getInstance() {
		return inst;
	}
}
