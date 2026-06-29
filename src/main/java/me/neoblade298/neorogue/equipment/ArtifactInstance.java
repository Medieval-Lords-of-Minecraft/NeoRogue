package me.neoblade298.neorogue.equipment;

import java.util.TreeMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

// need instance to keep track of things like amount and such which are normally
// handled by using itemstacks
public class ArtifactInstance implements Comparable<ArtifactInstance> {
	private Artifact artifact;
	private int amount;
	// Optional equipment "held" by this artifact instance. When present, the artifact
	// delegates trigger initialization to this equipment (see Artifact#initialize).
	private SessionEquipment held;
	// Synthetic slot assigned at fight initialization, used to give a held equipment a
	// unique trigger/stat namespace so multiple instances don't collide.
	private int slot;
	public ArtifactInstance(Artifact artifact, int amount) {
		this.artifact = artifact;
		this.amount = amount;
	}
	public ArtifactInstance(Artifact artifact) {
		this.artifact = artifact;
		this.amount = 1;
	}
	public ArtifactInstance(Artifact artifact, int amount, SessionEquipment held) {
		this.artifact = artifact;
		this.amount = amount;
		this.held = held;
	}
	public ArtifactInstance(Artifact artifact, SessionEquipment held) {
		this.artifact = artifact;
		this.amount = 1;
		this.held = held;
	}
	public Artifact getArtifact() {
		return artifact;
	}
	public SessionEquipment getHeld() {
		return held;
	}
	public boolean hasHeld() {
		return held != null;
	}
	public int getSlot() {
		return slot;
	}
	public void add(int amount) {
		this.amount+= amount;
	}
	public int getAmount() {
		return amount;
	}
	/**
	 * The key used to store this instance in the artifacts map. Holder artifacts (those
	 * with a held equipment) are keyed by their held equipment so that holders of
	 * different equipment remain separate entries while holders of the same equipment merge.
	 */
	public String getMapKey() {
		return held == null ? artifact.getId() : artifact.getId() + ":" + held.getEquipment().serialize();
	}
	public ItemStack getItem() {
		ItemStack item = held != null ? held.getItem() : artifact.getItem();
		if (amount > 1) {
			ItemMeta meta = item.getItemMeta();
			meta.setMaxStackSize(amount);
			item.setItemMeta(meta);
		}
		item.setAmount(amount);
		return item;
	}
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		this.slot = slot;
		this.artifact.initialize(data, this);
	}
	public void cleanup(PlayerFightData data) {
		this.artifact.cleanup(data);
	}
	@Override
	public int compareTo(ArtifactInstance o) {
		return getMapKey().compareTo(o.getMapKey());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((held == null) ? 0 : held.getEquipment().serialize().hashCode());
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
		if (held == null) {
			if (other.held != null) return false;
		}
		else if (other.held == null || !held.getEquipment().serialize().equals(other.held.getEquipment().serialize())) return false;
		return true;
	}
	
	public String serialize() {
		String base = artifact.getId() + (artifact.isUpgraded ? "+" : "") + "-" + amount;
		if (held != null) base += "-" + held.serialize();
		return base;
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
			map.put(inst.getMapKey(), inst);
		}
		return map;
	}
	
	public static ArtifactInstance deserialize(String str) {
		String[] aiPieces = str.split("-", 3);
		boolean isUpgraded = false;
		if (aiPieces[0].endsWith("+")) {
			isUpgraded = true;
			aiPieces[0] = aiPieces[0].substring(0, aiPieces[0].length() - 1);
		}
		Artifact a = (Artifact) Equipment.get(aiPieces[0], isUpgraded);
		SessionEquipment held = null;
		if (aiPieces.length >= 3 && !aiPieces[2].isBlank()) {
			held = SessionEquipment.deserialize(aiPieces[2]);
		}
		return new ArtifactInstance(a, Integer.parseInt(aiPieces[1]), held);
	}
}
