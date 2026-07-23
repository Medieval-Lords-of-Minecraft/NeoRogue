package me.neoblade298.neorogue.session.instances;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;

/**
 * Single source of truth for how saveable {@link Instance}s are serialized and reconstructed.
 * <p>
 * Each constant owns both its serialization key <em>and</em> the logic to rebuild the instance from
 * saved data, so the write side ({@code Instance#serialize}) and read side
 * ({@link Instance#deserialize}) can never drift apart: serializers must call {@link #key()} /
 * {@link #prefix()}, and {@link Instance#deserialize} dispatches through {@link #fromData(String)}.
 * Adding a new saveable instance is a single edit here.
 */
public enum InstanceType {
	// Order matters only for prefix matching in fromData(): a key must never be a prefix of another
	// key. Legacy aliases keep older saves loadable after a key is renamed (e.g. CAMPFIRE -> SHRINE).
	NODESELECT("NODESELECT", (s, data, party) -> NodeSelectInstance.create(s)),
	SHRINE("SHRINE", new String[] { "CAMPFIRE" }, (s, data, party) -> new ShrineInstance(s, data, party)),
	SHOP("SHOP", (s, data, party) -> new ShopInstance(s, party)),
	CHANCE("CHANCE", (s, data, party) -> new ChanceInstance(s, data, party)),
	// Covers every reward-room variant, including TREASURE nodes. The NodeType that produced the reward
	// is stored after the colon (e.g. "REWARD:TREASURE", "REWARD:FIGHT") and restored here.
	REWARD("REWARD", (s, data, party) -> new RewardInstance(s, party,
			NodeType.valueOf(data.substring(data.indexOf(':') + 1)), false));

	@FunctionalInterface
	public interface Deserializer {
		Instance deserialize(Session s, String data, HashMap<UUID, PlayerSessionData> party) throws SQLException;
	}

	private final String key;
	private final String[] legacyKeys;
	private final Deserializer deserializer;

	InstanceType(String key, Deserializer deserializer) {
		this(key, new String[0], deserializer);
	}

	InstanceType(String key, String[] legacyKeys, Deserializer deserializer) {
		this.key = key;
		this.legacyKeys = legacyKeys;
		this.deserializer = deserializer;
	}

	/** The canonical prefix used to identify this instance type in serialized data. */
	public String key() {
		return key;
	}

	/** Convenience for serializers that append a payload, e.g. {@code SHRINE.prefix() + state}. */
	public String prefix() {
		return key + ":";
	}

	public Instance deserialize(Session s, String data, HashMap<UUID, PlayerSessionData> party) throws SQLException {
		return deserializer.deserialize(s, data, party);
	}

	private boolean matches(String data) {
		if (data.startsWith(key)) return true;
		for (String legacy : legacyKeys) {
			if (data.startsWith(legacy)) return true;
		}
		return false;
	}

	/** Resolves the instance type from serialized data, or null if none matches (unknown/unsaveable). */
	public static InstanceType fromData(String data) {
		if (data == null) return null;
		for (InstanceType type : values()) {
			if (type.matches(data)) return type;
		}
		return null;
	}
}
