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
import me.neoblade298.neorogue.session.fight.trigger.event.StaminaChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MercenaryHeadband extends Artifact {
	private static final String ID = "mercenaryHeadband";
	private static final double regen = 0.5;
	public MercenaryHeadband() {
		super(ID, "Mercenary Headband", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.STAMINA_CHANGE, (pdata, in) -> {
			if (data.getStamina() + ((StaminaChangeEvent) in).getChange() < 50) return TriggerResult.keep();
			
			data.addStaminaRegen(regen);
			Util.msg(p, this.display.append(Component.text(" was activated", NamedTextColor.GRAY)));
			return TriggerResult.remove();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"Reaching <white>50</white> stamina in a fight increases your stamina regen by <white>" + regen + "</white>"
						+ " for the remainder of the fight.");
	}
}
