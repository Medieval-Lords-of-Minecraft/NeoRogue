package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.PoisonStatus;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class SerratedFencingSword extends Weapon {
	
	public SerratedFencingSword(boolean isUpgraded) {
		super("serratedFencingSword", isUpgraded, Rarity.UNCOMMON);
		display = "Serrated Fencing Sword";
		damage = 8;
		attackSpeed = 1;
		int shields = 7;
		int bleed = isUpgraded ? 3 : 2;
		item = Weapon.createItem(this, Material.STONE_SWORD, null, "&7On hit, grant yourself &e" + shields + "&7 shields. Apply &e" + bleed
				+ " &7bleed every 2 hits.");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			Damageable target = (Damageable) inputs[1];
			FightInstance.dealDamage(p, type, damage, target);
			FightInstance.getFightData(target.getUniqueId()).addStatus("POISON", new PoisonStatus());
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
