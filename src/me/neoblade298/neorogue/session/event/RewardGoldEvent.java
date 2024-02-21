package me.neoblade298.neorogue.session.event;

public class RewardGoldEvent {
	private int amount;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public RewardGoldEvent(int amount) {
		super();
		this.amount = amount;
	}
}
