package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ShieldbearerStaff extends Equipment {
	private static final String ID = "ShieldbearerStaff";
	private double mult;

	public ShieldbearerStaff(boolean isUpgraded) {
		super(
				ID, "Shieldbearer Staff", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(100, 0.5, 0.5, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		mult = isUpgraded ? 1.5 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			Player p = data.getPlayer();
			weaponSwing(p, data);
			weaponDamage(p, data, ev.getTarget(), properties.get(PropertyType.DAMAGE) + (data.getShields().getAmount() * mult));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.BLAZE_ROD,
				"Increases damage by " + DescUtil.yellow(mult + "x") + " of your current " + GlossaryTag.SHIELDS.tag(this) + ".");
	}
}
