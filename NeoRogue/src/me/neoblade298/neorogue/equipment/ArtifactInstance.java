package me.neoblade298.neorogue.equipment;

import java.util.TreeSet;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class ArtifactInstance implements Comparable<ArtifactInstance> {
	private Artifact artifact;
	private int amount;
	public ArtifactInstance(Artifact artifact, int amount) {
		this.artifact = artifact;
		this.amount = amount;
	}
	public ArtifactInstance(Artifact artifact) {
		this.artifact = artifact;
	}
	public Artifact getArtifact() {
		return artifact;
	}
	public int getAmount() {
		return amount;
	}
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		this.artifact.initialize(p, data, bind, slot);
	}
	public void cleanup(Player p, PlayerFightData data) {
		this.artifact.cleanup(p, data);
	}
	@Override
	public int compareTo(ArtifactInstance o) {
		return artifact.getId().compareTo(o.getArtifact().getId());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ArtifactInstance other = (ArtifactInstance) obj;
		if (amount != other.amount) return false;
		if (artifact == null) {
			if (other.artifact != null) return false;
		}
		else if (!artifact.equals(other.artifact)) return false;
		return true;
	}
	
	public String serialize() {
		return artifact.getId() + (artifact.isUpgraded ? "+" : "") + "-" + amount;
	}
	
	public static String serialize(TreeSet<ArtifactInstance> set) {
		String str = "";
		for (ArtifactInstance ai : set) {
			str += ai.serialize();
		}
		return str;
	}
	
	public static TreeSet<ArtifactInstance> deserializeSet(String str) {
		TreeSet<ArtifactInstance> set = new TreeSet<ArtifactInstance>();
		String[] separated = str.split(";");
		for (String aiString : separated) {
			if (aiString.isBlank()) continue;
			set.add(deserialize(aiString));
		}
		return set;
	}
	
	public static ArtifactInstance deserialize(String str) {
		String[] aiPieces = str.split("-");
		boolean isUpgraded = false;
		if (aiPieces[0].endsWith("+")) {
			isUpgraded = true;
			aiPieces[0] = aiPieces[0].substring(0, aiPieces[0].length() - 1);
		}
		Artifact a = (Artifact) Equipment.get(aiPieces[0], isUpgraded);
		return new ArtifactInstance(a, Integer.parseInt(aiPieces[1]));
	}
}
