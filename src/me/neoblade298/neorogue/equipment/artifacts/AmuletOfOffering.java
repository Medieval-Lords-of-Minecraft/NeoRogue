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
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AmuletOfOffering extends Artifact {
	private static final String ID = "amuletOfOffering";
	public AmuletOfOffering() {
		super(ID, "Amulet of Offering", Rarity.RARE, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.RECEIVED_HEALTH_DAMAGE, (pdata, in) -> {
			data.addMana(1000);
			data.addStamina(1000);
			data.addBuff(data, id, true, true, BuffType.GENERAL, 0.5, 300);
			Util.msg(p, this.display.append(Component.text(" was activated", NamedTextColor.GRAY)));
			return TriggerResult.remove();
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
		item = createItem(Material.SWEET_BERRIES, 
				"The first time you take health damage in a fight, max out your stamina and mana, and gain <white>50%</white> bonus damage for <white>15</white> seconds.");
	}
}
