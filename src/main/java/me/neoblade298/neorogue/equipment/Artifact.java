package me.neoblade298.neorogue.equipment;

import java.util.Set;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Artifact extends Equipment {
	protected boolean canStack; // If an artifact can be obtained from a player's droptable more than once

	// The nine post-fight gem artifacts (Ruby/Sapphire/Emerald x Shard/Cluster/Gem). They're earned
	// only through the post-fight gem choice and must never appear in drop pools or chance trades.
	private static final Set<String> GEM_ARTIFACT_IDS = Set.of(
			"RubyShard", "RubyCluster", "RubyGem",
			"SapphireShard", "SapphireCluster", "SapphireGem",
			"EmeraldShard", "EmeraldCluster", "EmeraldGem");

	public static boolean isGemArtifact(String id) {
		return GEM_ARTIFACT_IDS.contains(id);
	}

	public boolean isGemArtifact() {
		return GEM_ARTIFACT_IDS.contains(id);
	}

	public Artifact(String id, String display, Rarity rarity, EquipmentClass ec) {
		super(id, display, false, rarity, ec, EquipmentType.ARTIFACT, EquipmentProperties.none());
	}
	public Artifact(String id, String display, Rarity rarity, EquipmentClass[] ecs) {
		super(id, display, false, rarity, ecs, EquipmentType.ARTIFACT, EquipmentProperties.none());
	}

	public boolean canStack() {
		return canStack;
	}
	
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// This is not used, ArtifactInstance instead calls the below abstract initialize
	}
	public abstract void initialize(PlayerFightData data, ArtifactInstance ai);
	public abstract void onAcquire(PlayerSessionData data, int amount);
	public abstract void onInitializeSession(PlayerSessionData data);
	public void onRemove(PlayerSessionData data, int amount) {}
}
