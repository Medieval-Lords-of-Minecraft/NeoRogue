package me.neoblade298.neorogue.session.event;

import me.neoblade298.neorogue.area.NodeType;

public class RewardFightEvent {
	private int bonusGold, bonusEquipment;
	private NodeType type;

	public int getBonusGold() {
		return bonusGold;
	}

	public int getBonusEquipment() {
		return bonusEquipment;
	}

	public void addBonusGold(int goldAmount) {
		this.bonusGold += goldAmount;
	}

	public void addBonusDrops(int bonus) {
		this.bonusEquipment += bonus;
	}

	public NodeType getType() {
		return type;
	}

	public RewardFightEvent(NodeType type) {
		super();
		this.type = type;
	}
}
