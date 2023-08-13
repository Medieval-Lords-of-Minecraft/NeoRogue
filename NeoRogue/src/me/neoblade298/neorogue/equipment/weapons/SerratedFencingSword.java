package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.Shield;

public class SerratedFencingSword extends Weapon {
	private int bleed;
	private int shields;
	public SerratedFencingSword(boolean isUpgraded) {
		super("serratedFencingSword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.SWORDSMAN);
		display = "Serrated Fencing Sword";
		damage = 8;
		attackSpeed = 1;
		type = DamageType.PIERCING;
		shields = 7;
		bleed = isUpgraded ? 3 : 2;
		item = createItem(Material.STONE_SWORD, null, "&7On hit, grant yourself &e" + shields + "&7 shields. Apply &e" + bleed
				+ " &7bleed every 2 hits.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			Damageable target = (Damageable) inputs[1];
			FightInstance.dealDamage(p, type, damage, target);
			FightInstance.getFightData(target.getUniqueId()).applyStatus("BLEED", p.getUniqueId(), bleed, 0);
			data.getShields().addShield(new Shield(data, p.getUniqueId(), shields, true, 1, 100, 1, 1));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
