package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

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
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (ai.getAmount() == 1) {
				data.getSessionData().addMaxAbilities(1);
			}
			Util.msgRaw(p, hoverable.append(Component.text(" was removed from your inventory", NamedTextColor.GRAY)));
			data.getSessionData().removeArtifact(this);
			return TriggerResult.remove();
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
		item = createItem(Material.DEAD_FIRE_CORAL, "Decreases damage by <white>25%</white> for the first <white>20s</white>. Disappears after <white>1</white> fight.");
	}
}
