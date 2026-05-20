package me.neoblade298.neorogue.equipment;

import java.util.TreeMap;

import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

// need instance to keep track of things like amount and such which are normally
// handled by using itemstacks
public class ArtifactInstance implements Comparable<ArtifactInstance> {
	private Artifact artifact;
	private int amount;
	public ArtifactInstance(Artifact artifact, int amount) {
		this.artifact = artifact;
		this.amount = amount;
	}
	public ArtifactInstance(Artifact artifact) {
		this.artifact = artifact;
		this.amount = 1;
	}
	public Artifact getArtifact() {
		return artifact;
	}
	public void add(int amount) {
		this.amount+= amount;
	}
	public int getAmount() {
		return amount;
	}
	public ItemStack getItem() {
		ItemStack item = artifact.getItem();
		item.setAmount(amount);
		return item;
	}
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		this.artifact.initialize(data, this);
	}
	public void cleanup(PlayerFightData data) {
		this.artifact.cleanup(data);
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
	
	public static String serialize(TreeMap<String, ArtifactInstance> map) {
		String str = "";
		for (ArtifactInstance ai : map.values()) {
			str += ai.serialize() + ";";
		}
		return str;
	}
	
	public static TreeMap<String, ArtifactInstance> deserializeMap(String str) {
		TreeMap<String, ArtifactInstance> map = new TreeMap<String, ArtifactInstance>();
		String[] separated = str.split(";");
		for (String aiString : separated) {
			if (aiString.isBlank()) continue;
			ArtifactInstance inst = deserialize(aiString);
			map.put(inst.getArtifact().getId(), inst);
		}
		return map;
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
