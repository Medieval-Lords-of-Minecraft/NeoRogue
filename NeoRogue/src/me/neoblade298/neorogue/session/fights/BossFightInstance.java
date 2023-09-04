package me.neoblade298.neorogue.session.fights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.CoinsReward;
import me.neoblade298.neorogue.session.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.Reward;
import me.neoblade298.neorogue.session.RewardInstance;
import me.neoblade298.neorogue.session.Session;

public class BossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	
	public BossFightInstance(AreaType type) {
		super();
		map = Map.generateBoss(type, 2);
		targets.addAll(map.getTargets());
	}
	
	public BossFightInstance(Map map) {
		super();
		this.map = map;
		targets.addAll(map.getTargets());
	}

	@Override
	protected void setupInstance(Session s) {
		
	}
	
	public void handleMobKill(String id) {
		Mob mob = Mob.get(id);
		if (mob == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to find meta-info for mob " + id + " to handle mob kill");
			return;
		}
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			s.setInstance(new RewardInstance(generateRewards()));
			return;
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			list.add(new CoinsReward(100));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass().toEquipmentClass();
			int value = s.getAreasCompleted();
			equipDrops.addAll(Equipment.getDrop(value + 4, 3, ec));
			
			list.add(new EquipmentChoiceReward(equipDrops));
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(Equipment.get("rubyGem", false));
			equipDrops.add(Equipment.get("emeraldGem", false));
			equipDrops.add(Equipment.get("sapphireGem", false));
			list.add(new EquipmentChoiceReward(equipDrops));
			rewards.put(uuid, list);
		}
		return rewards;
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		s.broadcast("&7You beat the boss!");
	}

	@Override
	public String serializeInstanceData() {
		return "BOSS:" + map.serialize();
	}
}
