package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

// Generic "holder" artifact: stores a SessionEquipment on its ArtifactInstance and delegates
// trigger initialization to that equipment, granting its effects without occupying a slot.
public class EchoStone extends Artifact {
	public static final String ID = "EchoStone";
	// Offset so the hosted equipment's synthetic slot can't collide with real hotbar/accessory slots
	private static final int SLOT_OFFSET = 1000;

	public EchoStone() {
		super(ID, "Echo Stone", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false; // Only granted with a held equipment (e.g. via a chance event)
		canStack = false;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		SessionEquipment held = ai.getHeld();
		if (held == null) return;
		// Delegate to the held equipment, giving it a unique synthetic slot so its triggers
		// and stat trackers don't collide with the player's real copy or other holders.
		held.getEquipment().initialize(data, null, EquipSlot.ACCESSORY, SLOT_OFFSET + ai.getSlot(), held);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"Holds a piece of equipment and grants its effects without taking up an inventory slot.");
	}
}
