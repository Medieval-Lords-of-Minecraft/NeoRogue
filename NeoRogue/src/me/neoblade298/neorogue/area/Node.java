package me.neoblade298.neorogue.area;

import java.util.ArrayList;
import java.util.UUID;

import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fights.FightInstance;

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
		return type.toString().substring(0, 3);
	}
	
	public String serializePosition() {
		return pos + "," + lane;
	}
	
	public String serializeDestinations() {
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
	
	public void generateInstance(Session s) {
		switch (type) {
		case FIGHT: inst = new FightInstance(s);
		break;
		default:
			break;
		}
	}
	
	public void startInstance(Session s) {
		inst.start(s);
		
	}
}
