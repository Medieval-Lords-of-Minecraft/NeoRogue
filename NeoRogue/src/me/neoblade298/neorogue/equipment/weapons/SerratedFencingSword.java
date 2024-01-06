package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SerratedFencingSword extends Equipment {
	private int bleed;
	private int shields;
	public SerratedFencingSword(boolean isUpgraded) {
		super("serratedFencingSword", "Serrated Fencing Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(30, 1, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		shields = 4;
		bleed = isUpgraded ? 4 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			LivingEntity target = ev.getTarget();
			meleeWeapon(p, data, target);
			FightInstance.getFightData(target.getUniqueId()).applyStatus(StatusType.BLEED, p.getUniqueId(), bleed, 0);
			data.addShield(p.getUniqueId(), shields, true, 1, 100, 1, 1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "On hit, grant yourself <yellow>" + shields + "</yellow> shields. Apply <yellow>" + bleed
				+ " </yellow>bleed every 2 hits.");
	}
}
