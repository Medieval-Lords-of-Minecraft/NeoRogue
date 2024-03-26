package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BackupBattery extends Artifact {
	private static final String ID = "backupBattery";
	private static final int thresholdPercent = 25;
	private static final int regenPercent = 50;
	private boolean active;
	private double flatRegen;

	public BackupBattery() {
		super(ID, "Backup Battery", Rarity.RARE, EquipmentClass.MAGE);
		active = false;
		flatRegen = 0;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			boolean belowThreshold = pdata.getMana() < pdata.getMaxMana() * thresholdPercent / 100.0;
			if (!active && belowThreshold) {
				active = true;
				flatRegen = pdata.getStaminaRegen() * regenPercent / 100.0;
				pdata.addManaRegen(flatRegen);
				pdata.addStaminaRegen(-flatRegen);
			} else if (active && !belowThreshold) {
				active = false;
				pdata.addManaRegen(-flatRegen);
				pdata.addStaminaRegen(flatRegen);
				flatRegen = 0;
			}
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
				Material.POTION,
				"While below <white>" + thresholdPercent + "%</white> max mana, convert <white>" + regenPercent
						+ "%</white> of stamina regen to bonus mana regen."
		);
	}
}
