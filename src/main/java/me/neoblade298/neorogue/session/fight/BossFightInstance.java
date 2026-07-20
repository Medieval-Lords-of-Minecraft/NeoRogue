package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldGem;
import me.neoblade298.neorogue.equipment.artifacts.RubyGem;
import me.neoblade298.neorogue.equipment.artifacts.SapphireGem;
import me.neoblade298.neorogue.equipment.artifacts.TomeOfWisdom;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.instances.WinInstance;
import me.neoblade298.neorogue.session.reward.EquipmentReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardBuilder;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class BossFightInstance extends FightInstance {
	private HashSet<String> targets = new HashSet<String>();
	
	public BossFightInstance(Session s, Set<UUID> players, RegionType type) {
		super(s, players);
		map = Map.generateBoss(type, 0, s.isDebug());
		targets.addAll(map.getTargets());
		generateModifier(true);
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
	public void handleMobKill(FightData fd, String id, boolean playerKill) {
		if (!isActive) return;
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (targets.contains(id)) {
			targets.remove(id);
		}
		
		if (targets.isEmpty()) {
			s.awardXp(250);
			Title title = Title.title(Component.text("Victory"), Component.text(" "));

			String bossId = map.getPieces().get(0).getPiece().getId();
			for (PlayerSessionData psd : s.getParty().values()) {
				psd.trigger(SessionTrigger.WIN_BOSS, bossId);
			}

			RegionType bossRegion = s.getRegion().getType();
			// The region-completion caravan reward is no longer paid here; it's paid when the party
			// actually reaches the next region (RewardInstance's boss-completion transition) or, for the
			// final region, in WinInstance, so the reward message lands in a more visible spot.
			RegionType nextRegion = RegionType.getNextRegion(bossRegion, s.isEndless());
			if (nextRegion == null) {
				handleWin(title, new WinInstance(s));
			} else {
				handleWin(title, new RewardInstance(s, generateRewards(), NodeType.BOSS));
				s.generateNextRegion();
				s.setNode(s.getRegion().getNodes()[0][2]);
			}
		}
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		boolean dropPotion = s.rollPotionChance(10);
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			RewardBuilder rb = new RewardBuilder(s, data, NodeType.BOSS);
			int value = s.getBaseDropValue() + 4;

			rb.coins(100);
			rb.equipmentDrops(value, 3);
			rb.upgradeDrops(0);
			rb.artifacts(value, 3);
			rb.gems(RubyGem.get(), SapphireGem.get(), EmeraldGem.get());
			rb.extra(new EquipmentReward(new SessionEquipment(TomeOfWisdom.get())));
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

	@Override
	public String serializeInstanceData() {
		return serializeWithModifier("BOSS:");
	}

	@Override
	public void updateBoardLines() {
		
	}

	@Override
	public void handleMobDespawn(FightData fd, String id, boolean despawn, boolean playerKill) {

	}
}
