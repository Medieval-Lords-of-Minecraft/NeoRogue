package me.neoblade298.neorogue.session.fight;

import java.awt.Color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public enum FightScore {
	D(0, 9999, 5, null, new Color(255, 140, 90)),
	C(1, 20, 10, D, new Color(255, 178, 52)),
	B(2, 20, 15, C, new Color(255, 217, 52)),
	A(3, 20, 20, B, new Color(173, 214, 51)),
	S(4, 60, 25, A, new Color(160, 193, 90));
	
	private int value, threshold, coins; // Threshold in seconds
	private FightScore next;
	private String display;
	private Component comp;
	private FightScore(int value, int threshold, int coins, FightScore next, Color color) {
		this.value = value;
		this.threshold = threshold;
		this.next = next;
		this.coins = coins;
		this.display = ChatColor.of(color) + name();
		this.comp = Component.text(name(), TextColor.color(color.getRed(), color.getGreen(), color.getBlue()));
	}
	
	public int getValue() {
		return value;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	public int getCoins() {
		return coins;
	}
	
	public FightScore getNext() {
		return next;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public Component getComponentDisplay() {
		return comp;
	}
}
