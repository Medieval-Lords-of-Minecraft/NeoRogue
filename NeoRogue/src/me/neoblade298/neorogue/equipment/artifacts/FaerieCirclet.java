package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class FaerieCirclet extends Artifact {
	private int ticks;

	public FaerieCirclet(boolean isUpgraded) {
		super("faerieCirclet", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);

		display = "Faerie Circlet";
		ticks = isUpgraded ? 5 : 3;
		item = createItem(Material.EMERALD, "ARTIFACT",
				null, "Lengthens your invulnerability frames by <yellow>" + ticks + "</yellow> ticks. The default is " +
				"10 ticks, or 0.5 seconds.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		p.setNoDamageTicks(p.getNoDamageTicks() + ticks);
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}
	
	@Override
	public void cleanup(Player p, PlayerFightData data) {
		p.setNoDamageTicks(10);
	}
}
