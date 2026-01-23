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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BloodyTrinket extends Artifact {
	private static final String ID = "BloodyTrinket";
	private static final int str = 20;

	public BloodyTrinket() {
		super(ID, "Bloody Trinket", Rarity.UNCOMMON, EquipmentClass.WARRIOR);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		if (p.getHealth() <= data.getMaxHealth() * 0.5) {
			data.applyStatus(StatusType.STRENGTH, data, str, -1);
		}
		else {
			data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
				if (p.getHealth() <= data.getMaxHealth() * 0.5) {
					data.applyStatus(StatusType.STRENGTH, data, str, -1);
					return TriggerResult.remove();
				}
				return TriggerResult.keep();
			});
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, 
				"Being below <white>50%</white> health grants " + GlossaryTag.STRENGTH.tag(this, str, false) + ".");
	}
}
