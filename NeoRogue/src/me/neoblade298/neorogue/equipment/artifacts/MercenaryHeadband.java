package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MercenaryHeadband extends Artifact {
	private static final int regen = 2;
	public MercenaryHeadband() {
		super("mercenaryHeadband", "Mercenary Headband", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina()) return TriggerResult.keep();
			
			data.addStaminaRegen(regen);
			Util.msg(p, this.display.append(Component.text(" was activated", NamedTextColor.GRAY)));
			return TriggerResult.remove();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"Reaching max stamina in a fight increases your stamina regen by <white>" + regen + "</white>"
						+ " for the remainder of the fight.");
	}
}
