package me.neoblade298.neorogue.equipment;

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
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		this.artifact.initialize(p, data, bind, hotbar);
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
	
}
