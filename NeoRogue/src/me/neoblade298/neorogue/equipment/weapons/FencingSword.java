package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.Shield;

public class FencingSword extends Weapon {
	private int shields;
	
	public FencingSword(boolean isUpgraded) {
		super("fencingSword", isUpgraded, Rarity.UNCOMMON);
		display = "Fencing Sword";
		damage = isUpgraded ? 7 : 5;
		type = DamageType.PIERCING;
		attackSpeed = 1;
		shields = isUpgraded ? 6 : 4;
		item = Weapon.createItem(this, Material.STONE_SWORD, null, "&7On hit, grant yourself &e" + shields + "&7 shields");
		reforgeOptions.add("rapier");
		reforgeOptions.add("serratedFencingSword");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.getShields().addShield(new Shield(data, p.getUniqueId(), shields, true, 1, 100, 1, 1));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
