package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.Fissure;
import me.neoblade298.neorogue.equipment.abilities.RecklessSwing;
import me.neoblade298.neorogue.equipment.offhands.EnduranceShield;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class AvalonianMace extends Equipment {
	private static final String ID = "avalonianMace";
	private int mult;

	public AvalonianMace(boolean isUpgraded) {
		super(
				ID, "Avalonian Mace", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(100, 0.5, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		mult = isUpgraded ? 8 : 5;
	}

	@Override
	public void setupReforges() {
		addReforge(Fissure.get(), TheGreatDivide.get());
		addReforge(EnduranceShield.get(), ShieldbearerStaff.get());
		addReforge(RecklessSwing.get(), Bloodthirster.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwing(p, data);
			weaponDamage(p, data, ev.getTarget(), properties.get(PropertyType.DAMAGE) + data.getStatus(StatusType.STRENGTH).getStacks() * (mult - 1));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.MACE,
				"Affected by " + GlossaryTag.STRENGTH.tag(this) + " " + DescUtil.yellow(mult + "x")  + ".");
	}
}
