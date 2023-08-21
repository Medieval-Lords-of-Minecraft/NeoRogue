package me.neoblade298.neorogue.session.fights;

import java.awt.Color;

import net.md_5.bungee.api.ChatColor;

public enum FightScore {
	S(4, 99999, null, new Color(160, 193, 90)), // 65-999
	A(3, 15, S, new Color(173, 214, 51)), // 50-64
	B(2, 15, A, new Color(255, 217, 52)), // 35-49
	C(1, 15, B, new Color(255, 178, 52)), // 20-34
	D(0, 20, C, new Color(255, 140, 90)); // 0-19
	
	private int value, threshold;
	private FightScore next;
	private ChatColor color;
	private FightScore(int value, int threshold, FightScore next, Color color) {
		this.value = value;
		this.threshold = threshold;
		this.next = next;
		this.color = ChatColor.of(color);
	}
	
	public int getValue() {
		return value;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	public FightScore getNext() {
		return next;
	}
	
	public String getDisplay() {
		return color + this.name();
	}
}
