package me.neoblade298.neorogue.equipment.accessories;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;

public class DaedalusHammer extends Artifact {
	private static final String ID = "DaedalusHammer";
	
	public DaedalusHammer() {
		super(ID, "Daedalus Hammer", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canStack = true;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MACE, "Upgrades a random piece of equipment when obtained.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		Player p = data.getPlayer();
		Session s = data.getSession();
		ArrayList<EquipmentMetadata> list = data.aggregateEquipment((eq) -> { return !eq.isUpgraded() && eq.canUpgrade() && eq.getType() != EquipmentType.CONSUMABLE; });
		Collections.shuffle(list);
		if (list.isEmpty()) {
			Util.msgRaw(p, "You had nothing else to upgrade!");
			return;
		}
		EquipmentMetadata meta = list.getFirst();
		data.upgradeEquipment(meta.getEquipSlot(), meta.getSlot());
		p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
		Util.msgRaw(p, Component.text("You upgraded your ").append(meta.getEquipment().getHoverable()));
		s.broadcastOthers(SharedUtil.color("<yellow>" + p.getName() + "</yellow> upgraded their ").append(meta.getEquipment().getHoverable()), p);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}
}
