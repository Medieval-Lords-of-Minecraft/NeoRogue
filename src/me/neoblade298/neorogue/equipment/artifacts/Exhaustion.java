package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Exhaustion extends Artifact {
	private static final String ID = "Exhaustion";
	
	public Exhaustion() {
		super(ID, "Exhaustion", Rarity.COMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		canStack = true;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			data.getSessionData().removeArtifact(this);
			if (ai.getAmount() <= 0) {
				data.getSessionData().addMaxAbilities(1);
				return TriggerResult.remove();
			}
			Util.msgRaw(data.getPlayer(), hoverable.append(Component.text(" was removed from your inventory", NamedTextColor.GRAY)));
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addMaxAbilities(-1);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_FIRE_CORAL, "Decreases max abilities by <white>1</white> when you have at least one. " + 
			"Removes one of itself after <white>1</white> fight.");
	}
}
