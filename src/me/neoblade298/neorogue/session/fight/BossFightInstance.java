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
import me.neoblade298.neorogue.session.event.RewardGoldEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.CoinsReward;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.EquipmentReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;

public class BossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	
	public BossFightInstance(Session s, Set<UUID> players, AreaType type) {
		super(s, players);
		map = Map.generateBoss(type, 0);
		targets.addAll(map.getTargets());
	}
	
	public BossFightInstance(Session s, Set<UUID> players, Map map) {
		super(s, players);
		this.map = map;
		targets.addAll(map.getTargets());
	}
	
	public String getBossDisplay() {
		return map.getPieces().get(0).getPiece().getDisplay();
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
			s.broadcast("You beat the boss!");
			s.setInstance(new RewardInstance(s, generateRewards()));
			return;
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			RewardGoldEvent ev = new RewardGoldEvent(100);
			data.trigger(SessionTrigger.REWARD_GOLD, ev);
			list.add(new CoinsReward(ev.getAmount()));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getAreasCompleted();
			equipDrops.addAll(Equipment.getDrop(value + 3, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.addAll(Equipment.getArtifact(data.getArtifactDroptable(), value + 2, 4, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(Equipment.get("rubyGem", false));
			equipDrops.add(Equipment.get("emeraldGem", false));
			equipDrops.add(Equipment.get("sapphireGem", false));
			list.add(new EquipmentChoiceReward(equipDrops));
			list.add(new EquipmentReward(Equipment.get("tomeOfWisdom", false)));
			rewards.put(uuid, list);
		}
		return rewards;
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
	}

	@Override
	public String serializeInstanceData() {
		return "BOSS:" + map.serialize();
	}
}
