package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LionheartBangle extends Artifact {
	private static final String ID = "lionheartBangle";
	private int thres = 10;
	
	public LionheartBangle() {
		super(ID, "Lionheart Bangle", Rarity.RARE, EquipmentClass.WARRIOR);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		int str = (int) data.getMaxHealth() / thres;
		data.applyStatus(StatusType.STRENGTH, data, str, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_BLOCK, "For every " + DescUtil.white(thres) + " max HP you have, " +
		"gain " + GlossaryTag.STRENGTH.tag(this, 1, true) + " at the start of a fight.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}
}
