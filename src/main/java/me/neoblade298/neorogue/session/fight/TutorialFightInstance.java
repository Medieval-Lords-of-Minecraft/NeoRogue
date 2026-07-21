package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.abilities.EmpoweredEdge;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.reward.EquipmentReward;
import me.neoblade298.neorogue.session.reward.Reward;

public class TutorialFightInstance extends StandardFightInstance {

	// Stable per-fight key (the fight node's row) used to pick the hardcoded map + fixed reward. Threaded
	// in because fight instances are pre-generated 1-2 nodes ahead, so nodesVisited would be ambiguous.
	private int tutorialKey;

	public TutorialFightInstance(Session s, Set<UUID> players, RegionType type, int tutorialKey) {
		// Build a hardcoded map instead of the random one the standard constructor would generate. Using
		// the Map-accepting super constructor skips its random generation + modifier roll, so we generate
		// the modifier ourselves afterward to preserve the normal fight behavior.
		super(s, players, buildTutorialMap(s, type, tutorialKey));
		this.tutorialKey = tutorialKey;
		generateModifier(false);
	}

	public TutorialFightInstance(Session s, Set<UUID> players, Map map) {
		super(s, players, map);
		// Restored/deserialized fights have no node context; fall back to the live node count.
		this.tutorialKey = s.getNodesVisited();
	}

	// Hardcoded map-piece selection for the Meadowood tutorial fights, keyed by the fight node's row (a
	// stable value regardless of when the instance is pre-generated). Edit the lists below to control which
	// map pieces each tutorial fight uses; pieces are placed in listed order via entrance matching. Return
	// an empty list (or leave a case out) to fall back to normal random generation for that fight.
	private static List<String> getTutorialPieceIds(int tutorialKey) {
		switch (tutorialKey) {
		case 1: // First tutorial fight (row 1)
			return List.of("MDFight1");
		case 2: // Second tutorial fight (row 2)
			return List.of("MDFight2");
		default:
			return List.of();
		}
	}

	// Constructs the fight map from the hardcoded piece list for this tutorial key. Falls back to the
	// standard random generation if no pieces are configured or none can be resolved/placed, so the
	// tutorial is never left without a map.
	private static Map buildTutorialMap(Session s, RegionType type, int tutorialKey) {
		List<String> ids = getTutorialPieceIds(tutorialKey);
		if (ids == null || ids.isEmpty()) {
			return Map.generate(type, NeoRogue.gen.nextInt(3, 6), s.isDebug());
		}

		Map map = new Map(type);
		int placed = 0;
		for (String id : ids) {
			MapPiece piece = MapPiece.get(id);
			if (piece == null) {
				Bukkit.getLogger().warning("[NeoRogue] Tutorial map piece not found: " + id);
				continue;
			}
			if (map.place(piece)) placed++;
			else Bukkit.getLogger().warning("[NeoRogue] Could not place tutorial map piece: " + id);
		}

		if (placed == 0) {
			Bukkit.getLogger().warning("[NeoRogue] No tutorial pieces placed for tutorialKey=" + tutorialKey
					+ "; falling back to random generation.");
			return Map.generate(type, NeoRogue.gen.nextInt(3, 6), s.isDebug());
		}

		// numPieces = 0 adds no random pieces; this call just finalizes the map (postGenerate).
		return Map.generate(map, type, 0, s.isDebug());
	}

	@Override
	protected void setupInstance(Session s) {
		super.setupInstance(s);
		scoreRequired = Math.ceil(scoreRequired / 4);
	}

	@Override
	protected double getInitialSpawnBudget() {
		return 2;
	}

	// Tutorial fights hand out a fixed, deterministic reward (a single Empowered Edge, no coins) so the
	// reward screen always shows the same thing regardless of fight score.
	@Override
	protected HashMap<UUID, ArrayList<Reward>> generateRewards(Session s, FightScore fightScore) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();

		if (tutorialKey == 1) {
			for (UUID uuid : s.getParty().keySet()) {
				ArrayList<Reward> playerRewards = new ArrayList<Reward>();
				playerRewards.add(new EquipmentReward(new SessionEquipment(EmpoweredEdge.get())));
				rewards.put(uuid, playerRewards);
			}
		}
		return rewards;
	}
}

