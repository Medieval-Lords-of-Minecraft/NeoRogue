package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;

public class SoulCarton extends Artifact {
	private static final String ID = "soulCarton";
	
	public SoulCarton() {
		super(ID, "Soul Carton", Rarity.UNCOMMON, EquipmentClass.MAGE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.RECEIVED_HEALTH_DAMAGE, (pdata, in) -> {
			ReceivedHealthDamageEvent ev = (ReceivedHealthDamageEvent) in;
			if (ev.getDamage() > pdata.getHealth()) {
				pdata.addSimpleShield(p.getUniqueId(), pdata.getMana(), 200);
				pdata.setMana(0);
				return TriggerResult.of(true, true);
			} else
				return TriggerResult.keep();
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
		item = createItem(
				Material.BONE_MEAL,
				"The first time you would die each fight, instead convert remaining mana to "
						+ GlossaryTag.SHIELDS.tag(this) + " for <white>10</white> seconds."
		);
	}
}
