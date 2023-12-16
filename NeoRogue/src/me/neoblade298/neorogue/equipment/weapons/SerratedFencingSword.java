package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SerratedFencingSword extends Weapon {
	private int bleed;
	private int shields;
	public SerratedFencingSword(boolean isUpgraded) {
		super("serratedFencingSword", "Serrated Fencing Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = 60;
		attackSpeed = 1;
		type = DamageType.PIERCING;
		shields = 7;
		bleed = isUpgraded ? 4 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Damageable target = (Damageable) inputs[1];
			FightInstance.dealDamage(p, type, damage, target);
			FightInstance.getFightData(target.getUniqueId()).applyStatus(StatusType.BLEED, p.getUniqueId(), bleed, 0);
			data.addShield(p.getUniqueId(), shields, true, 1, 100, 1, 1);
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, null, "On hit, grant yourself <yellow>" + shields + "</yellow> shields. Apply <yellow>" + bleed
				+ " </yellow>bleed every 2 hits.");
	}
}
