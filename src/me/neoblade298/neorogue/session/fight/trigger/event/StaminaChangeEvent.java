package me.neoblade298.neorogue.session.fight.trigger.event;

public class StaminaChangeEvent {
	private double change;

	public StaminaChangeEvent(double change) {
		this.change = change;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}
	
}
