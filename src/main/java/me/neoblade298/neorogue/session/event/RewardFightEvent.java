package me.neoblade298.neorogue.session.event;

import me.neoblade298.neorogue.region.NodeType;

public class RewardFightEvent {
	private int bonusGold, bonusEquipment, bonusRarity;
	private double bonusUpgradeChance;
	private NodeType type;

	public int getBonusGold() {
		return bonusGold;
	}

	public int getBonusEquipment() {
		return bonusEquipment;
	}

	public int getBonusRarity() {
		return bonusRarity;
	}

	public double getBonusUpgradeChance() {
		return bonusUpgradeChance;
	}

	public void addBonusGold(int goldAmount) {
		this.bonusGold += goldAmount;
	}

	public void addBonusDrops(int bonus) {
		this.bonusEquipment += bonus;
	}

	public void addBonusRarity(int bonus) {
		this.bonusRarity += bonus;
	}

	public void addBonusUpgradeChance(double chance) {
		this.bonusUpgradeChance = chance;
	}

	public NodeType getType() {
		return type;
	}

	public RewardFightEvent(NodeType type) {
		super();
		this.type = type;
	}
}
