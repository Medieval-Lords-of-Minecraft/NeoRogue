package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardBuilder;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class MinibossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	private String minibossId;
	
	public MinibossFightInstance(Session s, Set<UUID> party, RegionType type) {
		super(s, party);
		map = Map.generateMiniboss(type, 0, s.isDebug(), s.getLastMiniboss());
		minibossId = map.getPieces().getFirst().getPiece().getId();
		targets.addAll(map.getTargets());
	}
	
	public MinibossFightInstance(Session s, Set<UUID> party, Map map) {
		super(s, party);
		this.map = map;
		minibossId = map.getPieces().getFirst().getPiece().getId();
		targets.addAll(map.getTargets());
	}

	@Override
	protected void setupInstance(Session s) {
		s.setLastMiniboss(minibossId);
	}
	
	@Override
	public void handleMobKill(FightData fd, String id, boolean playerKill) {
		if (!isActive) return;
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			s.awardXp(120);
			Title title = Title.title(Component.text("Victory"), Component.text(" "));
			handleWin(title, new RewardInstance(s, generateRewards(), NodeType.MINIBOSS));
			for (PlayerSessionData psd : s.getParty().values()) {
				psd.trigger(SessionTrigger.WIN_MINIBOSS, minibossId);
			}
			return;
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		boolean dropPotion = s.rollPotionChance(15);
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			RewardBuilder rb = new RewardBuilder(s, data, NodeType.MINIBOSS);
			int value = s.getBaseDropValue() + 2;

			rb.coins(50);
			rb.equipmentDrops(value, 3);
			rb.upgradeDrops(0);
			rb.artifacts(value, 1);
			rb.gems(RubyCluster.get(), SapphireCluster.get(), EmeraldCluster.get());
			if (dropPotion) {
				rb.consumable(value, 0.1);
			}

			rewards.put(uuid, rb.build());
		}
		return rewards;
	}
	
	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
	}
	
	public String serializeInstanceData() {
		return "MINIBOSS:" + map.serialize();
	}

	@Override
	public void updateBoardLines() {
		
	}

	@Override
	public void handleMobDespawn(FightData fd, String id, boolean despawn, boolean playerKill) {

	}
}
