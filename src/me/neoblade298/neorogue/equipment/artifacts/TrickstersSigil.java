package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
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

public class TrickstersSigil extends Artifact {
	private static final String ID = "trickstersSigil";
	
	public TrickstersSigil() {
		super(ID, "Trickster's Sigil", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			Sounds.breaks.play(p, p);
			Util.msgRaw(p, display.append(Component.text(" was used", NamedTextColor.GRAY)));
			return TriggerResult.of(true, true);
		});
	}
	
	@Override
	public void onAcquire(PlayerSessionData data) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.SKELETON_SKULL, "Prevents the first instance of taking damage in a fight.");
	}
}