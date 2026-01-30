package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldGem;
import me.neoblade298.neorogue.equipment.artifacts.RubyGem;
import me.neoblade298.neorogue.equipment.artifacts.SapphireGem;
import me.neoblade298.neorogue.equipment.artifacts.TomeOfWisdom;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.CoinsReward;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.EquipmentReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class BossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	
	public BossFightInstance(Session s, Set<UUID> players, RegionType type) {
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
	public void handleMobKill(String id, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			Title title = Title.title(Component.text("Victory"), Component.text(" "));
			handleWin(title, new RewardInstance(s, generateRewards(), NodeType.BOSS));
			
			// Set up next region
			s.generateNextRegion();
			s.setNode(s.getRegion().getNodes()[0][2]);
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
			list.add(new CoinsReward((int) ((1 - (s.getCoinReduction() * Session.COIN_REDUCTION_PER_LEVEL)) * 100) + ev.getBonusGold()));

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
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
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
