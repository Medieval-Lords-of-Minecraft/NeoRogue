package me.neoblade298.neorogue.session.fight.trigger.event;

public class ManaChangeEvent {
	private double change;

	public ManaChangeEvent(double change) {
		this.change = change;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}
	
}
