package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class CrystallineFlask extends Artifact {
	private static final String ID = "crystallineFlask";
	private int healthPercent = 10;
	public CrystallineFlask() {
		super(ID, "Crystalline Flask", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		if (data.getInstance() instanceof StandardFightInstance) return;
		data.addHealth(data.getMaxHealth() * 0.01 * healthPercent);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Heal for <white>10%</white> of your max health when you start a fight against a miniboss or boss.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(11, 112, 227));
		item.setItemMeta(meta);
	}
}
