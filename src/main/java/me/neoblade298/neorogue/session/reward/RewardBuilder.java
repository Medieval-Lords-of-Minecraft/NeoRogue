package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.settings.NotorietySetting;

public class RewardBuilder {
	private final Session s;
	private final PlayerSessionData data;
	private final RewardFightEvent ev;
	private final ArrayList<Reward> rewards = new ArrayList<>();
	private final ArrayList<Equipment> equipDrops = new ArrayList<>();

	public RewardBuilder(Session s, PlayerSessionData data, NodeType type) {
		this.s = s;
		this.data = data;
		this.ev = new RewardFightEvent(type);
		data.trigger(SessionTrigger.REWARD_FIGHT, ev);
	}

	public RewardBuilder coins(int base) {
		int coins = base + ev.getBonusGold();
		if (NotorietySetting.REDUCE_COINS.isActive(s)) {
			coins = (int) (coins * NotorietySetting.REDUCE_COINS_MULTIPLIER);
		}
		rewards.add(new CoinsReward(coins));
		return this;
	}

	public RewardBuilder equipmentDrops(int value, int count) {
		EquipmentClass ec = data.getPlayerClass();
		int finalValue = value + ev.getBonusRarity();
		int finalCount = count + ev.getBonusEquipment();
		equipDrops.addAll(Equipment.getDrop(data.getData().getEquipmentDroptable(), finalValue, finalCount, ec, EquipmentClass.CLASSLESS));
		return this;
	}

	public RewardBuilder equipmentDrops(int value, int count, ArrayList<Equipment> exclusions) {
		EquipmentClass ec = data.getPlayerClass();
		int finalValue = value + ev.getBonusRarity();
		int finalCount = count + ev.getBonusEquipment();
		equipDrops.addAll(Equipment.getDrop(data.getData().getEquipmentDroptable(), finalValue, finalCount, exclusions, ec, EquipmentClass.CLASSLESS));
		return this;
	}

	/**
	 * Add equipment drops without applying event bonus rarity or count.
	 * Used for higher-tier drops (e.g., value+1 drops in S/A/B tiers).
	 */
	public RewardBuilder equipmentDropsRaw(int value, int count) {
		EquipmentClass ec = data.getPlayerClass();
		equipDrops.addAll(Equipment.getDrop(data.getData().getEquipmentDroptable(), value, count, ec, EquipmentClass.CLASSLESS));
		return this;
	}

	/**
	 * Add equipment drops without applying event bonuses, with exclusions.
	 */
	public RewardBuilder equipmentDropsRaw(int value, int count, ArrayList<Equipment> exclusions) {
		EquipmentClass ec = data.getPlayerClass();
		equipDrops.addAll(Equipment.getDrop(data.getData().getEquipmentDroptable(), value, count, exclusions, ec, EquipmentClass.CLASSLESS));
		return this;
	}

	public RewardBuilder upgradeDrops(double bonusChance) {
		s.rollUpgrades(equipDrops, bonusChance + ev.getBonusUpgradeChance());
		rewards.add(new EquipmentChoiceReward(new ArrayList<>(equipDrops)));
		equipDrops.clear();
		return this;
	}

	public RewardBuilder artifacts(int value, int count) {
		EquipmentClass ec = data.getPlayerClass();
		ArrayList<Equipment> arts = new ArrayList<>(Equipment.getArtifact(data.getArtifactDroptable(), value, count, ec, EquipmentClass.CLASSLESS));
		if (arts.size() == 1) {
			rewards.add(new EquipmentReward(arts.get(0)));
		} else {
			rewards.add(new EquipmentChoiceReward(arts));
		}
		return this;
	}

	public RewardBuilder consumable(int value, double bonusUpgradeChance) {
		EquipmentClass ec = data.getPlayerClass();
		Consumable cons = Equipment.getConsumable(value, ec, EquipmentClass.CLASSLESS);
		rewards.add(new EquipmentReward(s.rollUpgrade(cons, bonusUpgradeChance + ev.getBonusUpgradeChance())));
		return this;
	}

	public RewardBuilder gems(Equipment... gems) {
		ArrayList<Equipment> gemList = new ArrayList<>(gems.length);
		for (Equipment gem : gems) {
			gemList.add(gem);
		}
		rewards.add(new EquipmentChoiceReward(gemList));
		return this;
	}

	public RewardBuilder extra(Reward reward) {
		rewards.add(reward);
		return this;
	}

	public ArrayList<Reward> build() {
		return rewards;
	}

	public ArrayList<Equipment> getEquipDrops() {
		return equipDrops;
	}

	public int getBaseValue() {
		return s.getBaseDropValue() + ev.getBonusRarity();
	}
}
