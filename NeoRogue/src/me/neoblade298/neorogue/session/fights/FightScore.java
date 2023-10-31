package me.neoblade298.neorogue.session.fights;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public enum FightScore {
	D(0, 9999, 5, null, TextColor.color(255, 140, 90)),
	C(1, 20, 10, D, TextColor.color(255, 178, 52)),
	B(2, 20, 15, C, TextColor.color(255, 217, 52)),
	A(3, 20, 20, B, TextColor.color(173, 214, 51)),
	S(4, 60, 25, A, TextColor.color(160, 193, 90));
	
	private int value, threshold, coins; // Threshold in seconds
	private FightScore next;
	private Component display;
	private FightScore(int value, int threshold, int coins, FightScore next, TextColor color) {
		this.value = value;
		this.threshold = threshold;
		this.next = next;
		this.coins = coins;
		this.display = Component.text(name(), color);
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
	
	public Component getDisplay() {
		return display;
	}
}
