package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.NeoRogue;

public class PlayerAttributeController {
	public static final String GRAVITY = "gravity";
	public static final String JUMP = "jump";
	public static final String WITHERED = "withered";

	private static final Map<UUID, PlayerAttributeController> active = new HashMap<UUID, PlayerAttributeController>();
	private static final Map<String, Attribute> knownModifiers = Map.of(
			GRAVITY, Attribute.GRAVITY,
			JUMP, Attribute.JUMP_STRENGTH,
			WITHERED, Attribute.JUMP_STRENGTH);

	private final UUID playerId;
	private final Map<Attribute, Double> baseValues = new HashMap<Attribute, Double>();
	private final Map<String, Attribute> modifiers = new HashMap<String, Attribute>();
	private final Map<String, UUID> modifierVersions = new HashMap<String, UUID>();

	public PlayerAttributeController(UUID playerId) {
		this.playerId = playerId;
		PlayerAttributeController previous = active.put(playerId, this);
		if (previous != null) previous.restore();
	}

	public void setBaseValue(Attribute attribute, double value) {
		AttributeInstance instance = getAttribute(attribute);
		if (instance == null) return;
		baseValues.putIfAbsent(attribute, instance.getBaseValue());
		instance.setBaseValue(value);
	}

	public boolean hasModifier(String id) {
		Attribute attribute = modifiers.get(id);
		AttributeInstance instance = getAttribute(attribute);
		return instance != null && instance.getModifier(key(id)) != null;
	}

	public void applyModifier(String id, Attribute attribute, double amount, Operation operation) {
		removeModifier(id);
		AttributeInstance instance = getAttribute(attribute);
		if (instance == null) return;
		instance.addTransientModifier(new AttributeModifier(key(id), amount, operation));
		modifiers.put(id, attribute);
		modifierVersions.put(id, UUID.randomUUID());
	}

	public void applyTimedModifier(PlayerFightData data, String id, Attribute attribute, double amount,
			Operation operation, int ticks) {
		applyModifier(id, attribute, amount, operation);
		UUID version = modifierVersions.get(id);
		if (version == null) return;
		data.addGuaranteedTask(UUID.randomUUID(), () -> {
			if (version.equals(modifierVersions.get(id))) removeModifier(id);
		}, ticks);
	}

	public void applyTimedValue(PlayerFightData data, String id, Attribute attribute, double value, int ticks) {
		removeModifier(id);
		AttributeInstance instance = getAttribute(attribute);
		if (instance == null) return;
		applyTimedModifier(data, id, attribute, value - instance.getValue(), Operation.ADD_NUMBER, ticks);
	}

	public void removeModifier(String id) {
		modifierVersions.remove(id);
		Attribute attribute = modifiers.remove(id);
		if (attribute == null) attribute = knownModifiers.get(id);
		AttributeInstance instance = getAttribute(attribute);
		if (instance != null) instance.removeModifier(key(id));
	}

	public void clearFightModifiers() {
		for (String id : modifiers.keySet().toArray(String[]::new)) {
			removeModifier(id);
		}
	}

	public void restore() {
		clearFightModifiers();
		for (Map.Entry<Attribute, Double> entry : baseValues.entrySet()) {
			AttributeInstance instance = getAttribute(entry.getKey());
			if (instance != null) instance.setBaseValue(entry.getValue());
		}
	}

	public static void reset(Player player) {
		PlayerAttributeController controller = active.remove(player.getUniqueId());
		if (controller != null) {
			controller.restore();
			return;
		}

		for (Map.Entry<String, Attribute> entry : knownModifiers.entrySet()) {
			AttributeInstance instance = player.getAttribute(entry.getValue());
			if (instance != null) instance.removeModifier(key(entry.getKey()));
		}
		AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
		if (maxHealth != null) maxHealth.setBaseValue(Attribute.MAX_HEALTH.getDefaultValue());
	}

	private AttributeInstance getAttribute(Attribute attribute) {
		Player player = Bukkit.getPlayer(playerId);
		return player == null || attribute == null ? null : player.getAttribute(attribute);
	}

	private static NamespacedKey key(String id) {
		return new NamespacedKey(NeoRogue.inst(), id);
	}
}