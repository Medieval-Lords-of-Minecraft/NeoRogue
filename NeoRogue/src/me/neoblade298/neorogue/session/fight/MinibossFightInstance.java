package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.reward.CoinsReward;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;

public class MinibossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	
	public MinibossFightInstance(Session s, Set<UUID> party, AreaType type) {
		super(s, party);
		map = Map.generateMiniboss(type, 0);
		targets.addAll(map.getTargets());
	}
	
	public MinibossFightInstance(Session s, Set<UUID> party, Map map) {
		super(s, party);
		this.map = map;
		targets.addAll(map.getTargets());
	}

	@Override
	protected void setupInstance(Session s) {
		
	}
	
	@Override
	public void handleMobKill(String id) {
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			FightInstance.handleWin();
			s.broadcast("You beat the miniboss!");
			s.setInstance(new RewardInstance(s, generateRewards()));
			return;
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			list.add(new CoinsReward(50));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getAreasCompleted();
			equipDrops.addAll(Equipment.getDrop(value + 2, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.addAll(Equipment.getArtifact(value + 2, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(Equipment.get("rubyCluster", false));
			equipDrops.add(Equipment.get("emeraldCluster", false));
			equipDrops.add(Equipment.get("sapphireCluster", false));
			list.add(new EquipmentChoiceReward(equipDrops));
			rewards.put(uuid, list);
		}
		return rewards;
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
	}
	
	public String serializeInstanceData() {
		return "MINIBOSS:" + map.serialize();
	}
}
