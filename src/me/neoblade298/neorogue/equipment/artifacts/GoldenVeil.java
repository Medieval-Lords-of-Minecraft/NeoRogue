package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GoldenVeil extends Artifact {
	private static final String ID = "GoldenVeil";
	
	public GoldenVeil() {
		super(ID, "Golden Veil", Rarity.RARE, EquipmentClass.MAGE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() > pdata.getMaxMana() * 0.8) {
				am.addCount(1);
				if (am.getCount() >= 3) {
					am.addCount(-3);
					data.addPermanentShield(p.getUniqueId(), 1);
				}
			}
			return TriggerResult.keep();
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
		item = createItem(Material.CLOCK, "Grants " + GlossaryTag.SHIELDS.tag(this, 1, false) + " for every <white>3s</white> you're at above <white>80%</white> mana.");
	}
}
