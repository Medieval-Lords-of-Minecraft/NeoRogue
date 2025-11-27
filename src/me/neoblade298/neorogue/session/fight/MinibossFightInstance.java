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
import me.neoblade298.neorogue.area.NodeType;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
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
					s.setInstance(new RewardInstance(s, generateRewards(), NodeType.MINIBOSS));
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
			RewardFightEvent ev = new RewardFightEvent(NodeType.MINIBOSS);
			data.trigger(SessionTrigger.REWARD_FIGHT, ev);
			list.add(new CoinsReward((int) ((1 - s.getGoldReduction()) * 50) + ev.getBonusGold()));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getBaseDropValue() + 2 + ev.getBonusRarity();
			equipDrops.addAll(Equipment.getDrop(value, 3 + ev.getBonusEquipment(), ec, EquipmentClass.CLASSLESS));
			s.rollUpgrades(equipDrops, 0.1);
			list.add(new EquipmentChoiceReward(equipDrops));
			
			Artifact art = Equipment.getArtifact(data.getArtifactDroptable(), value, 3, ec, EquipmentClass.CLASSLESS).getFirst();
			list.add(new EquipmentReward(art));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(RubyCluster.get());
			equipDrops.add(EmeraldCluster.get());
			equipDrops.add(SapphireCluster.get());
			list.add(new EquipmentChoiceReward(equipDrops));
			if (dropPotion) {
				Consumable cons = Equipment.getConsumable(value, ec, EquipmentClass.CLASSLESS);
				list.add(new EquipmentReward(s.rollUpgrade(cons, 0.1 + ev.getBonusUpgradeChance())));
			}
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
	public void handlePlayerLeaveParty(Player p) {
		
	}

	@Override
	public void updateBoardLines() {

	}
}
