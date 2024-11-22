package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
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
	public void handleMobKill(String id, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			new BukkitRunnable() {
				public void run() {
					FightInstance.handleWin();
					s.broadcast("You beat the miniboss!");
					s.setInstance(new RewardInstance(s, generateRewards()));
				}
			}.runTask(NeoRogue.inst());
			return;
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		boolean dropPotion = false;
		if (NeoRogue.gen.nextInt(100) < s.getPotionChance()) {
			s.addPotionChance(-25);
			dropPotion = true;
		}
		else {
			s.addPotionChance(15);
		}
		
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			RewardGoldEvent ev = new RewardGoldEvent(50);
			data.trigger(SessionTrigger.REWARD_GOLD, ev);
			list.add(new CoinsReward(ev.getAmount()));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getAreasCompleted() + 2;
			equipDrops.addAll(Equipment.getDrop(value, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.addAll(Equipment.getArtifact(data.getArtifactDroptable(), value, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(Equipment.get("rubyCluster", false));
			equipDrops.add(Equipment.get("emeraldCluster", false));
			equipDrops.add(Equipment.get("sapphireCluster", false));
			list.add(new EquipmentChoiceReward(equipDrops));
			if (dropPotion) list.add(new EquipmentReward(Equipment.getConsumable(value, ec, EquipmentClass.CLASSLESS)));
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

	@Override
	public void addSpectator(Player p) {
		
	}

	@Override
	public void removeSpectator(Player p) {
		
	}

	@Override
	public void handlePlayerKickEvent(Player kicked) {
		
	}
}
