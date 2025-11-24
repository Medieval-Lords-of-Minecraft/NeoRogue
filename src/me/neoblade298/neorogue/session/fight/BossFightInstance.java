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
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldGem;
import me.neoblade298.neorogue.equipment.artifacts.RubyGem;
import me.neoblade298.neorogue.equipment.artifacts.SapphireGem;
import me.neoblade298.neorogue.equipment.artifacts.TomeOfWisdom;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
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

	public BossFightInstance(Session s, Set<UUID> players, AreaType type, String boss) {
		super(s, players);
		MapPiece piece = MapPiece.get(boss);
		map = Map.generate(type, 0, piece);
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

	public String getPieceId() {
		return map.getPieces().get(0).getPiece().getId();
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
					s.broadcast("You beat the boss!");
					s.setInstance(new RewardInstance(s, generateRewards()));
					
					// Set up next area
					s.generateNextArea();
					s.setNode(s.getArea().getNodes()[0][2]);
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
			s.addPotionChance(10);
		}
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			RewardFightEvent ev = new RewardFightEvent(NodeType.BOSS);
			data.trigger(SessionTrigger.REWARD_FIGHT, ev);
			list.add(new CoinsReward(100 + ev.getBonusGold()));

			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getBaseDropValue() + 4 + ev.getBonusRarity();
			equipDrops.addAll(Equipment.getDrop(value, 3 + ev.getBonusEquipment(), ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.addAll(Equipment.getArtifact(data.getArtifactDroptable(), value, 3, ec, EquipmentClass.CLASSLESS));
			list.add(new EquipmentChoiceReward(equipDrops));
			
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(RubyGem.get());
			equipDrops.add(EmeraldGem.get());
			equipDrops.add(SapphireGem.get());
			list.add(new EquipmentChoiceReward(equipDrops));
			list.add(new EquipmentReward(TomeOfWisdom.get()));
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

	@Override
	public String serializeInstanceData() {
		return "BOSS:" + map.serialize();
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
