package me.neoblade298.neorogue.session.event;

public class GenerateShopEvent {
	private double discount;

	public double getDiscountMultiplier() {
		return 1 - discount;
	}

	public void addDiscount(double discount) {
		this.discount += discount;
	}

	public GenerateShopEvent() {
		super();
	}
}
