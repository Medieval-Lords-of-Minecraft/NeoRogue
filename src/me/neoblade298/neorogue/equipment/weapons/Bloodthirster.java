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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Bloodthirster extends Equipment {
	private static final String ID = "bloodthirster";
	private int mult;

	public Bloodthirster(boolean isUpgraded) {
		super(
				ID, "Bloodthirster", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(150, 0.5, 0.5, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		mult = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			int bonus = (data.getStatus(StatusType.BERSERK).getStacks() * mult) + (data.getStatus(StatusType.STRENGTH).getStacks() * (mult - 1));
			weaponSwing(p, data);
			weaponDamage(p, data, ev.getTarget(), properties.get(PropertyType.DAMAGE) + bonus);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.IRON_SWORD,
				"Increases its damage by " + DescUtil.yellow(mult + "x") + " "+ GlossaryTag.STRENGTH.tag(this) + " and " + GlossaryTag.BERSERK.tag(this) + ".");
	}
}
