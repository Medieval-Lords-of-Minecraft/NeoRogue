package me.neoblade298.neorogue.session.fight;

import java.awt.Color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public enum FightScore {
	D(0, 9999, 5, 0.0, 1, null, new Color(255, 140, 90)),
	C(1, 50, 10, 0.0, 5, D, new Color(255, 178, 52)),
	B(2, 45, 15, 0.0, 8, C, new Color(255, 217, 52)),
	A(3, 40, 20, 0.05, 10, B, new Color(173, 214, 51)),
	S(4, 80, 25, 0.1, 11, A, new Color(160, 193, 90));

	private int value, threshold, coins, xp; // Threshold in seconds
	private double upgradeModifier;
	private FightScore next;
	private String miniMessageDisplay;
	private Component comp;
	private FightScore(int value, int threshold, int coins, double upgradeModifier, int xp, FightScore next, Color color) {
		this.value = value;
		this.threshold = threshold;
		this.next = next;
		this.coins = coins;
		this.xp = xp;
		this.upgradeModifier = upgradeModifier;
		this.miniMessageDisplay = "<color:#" + String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) + ">" + name() + "</color>";
		this.comp = Component.text(name(), TextColor.color(color.getRed(), color.getGreen(), color.getBlue()));
	}
	
	public String getMiniMessageDisplay() {
		return miniMessageDisplay;
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

	public double getUpgradeModifier() {
		return upgradeModifier;
	}
	
	public FightScore getNext() {
		return next;
	}
	
	public Component getComponentDisplay() {
		return comp;
	}

	public int getXp() {
		return xp;
	}
}
