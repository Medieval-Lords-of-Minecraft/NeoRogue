package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Accessory;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class EarthenRing extends Accessory {
	private int damage;
	
	public EarthenRing(boolean isUpgraded) {
		super("earthenRing", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		damage = isUpgraded ? 3 : 2;
		display = "Earthen Ring";
		item = createItem(Material.GOLD_NUGGET, "ACCESSORY", reforgeOptions, "Basic attacks additionally deal <yellow>" + damage + "</yellow> damage.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			Damageable target = (Damageable) in[1];
			FightInstance.dealDamage(p, DamageType.EARTH, damage, target);
			return false;
		});
	}
}
