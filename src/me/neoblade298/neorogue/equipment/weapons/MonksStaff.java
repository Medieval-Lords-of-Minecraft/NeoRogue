package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.EnduranceTraining;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class MonksStaff extends Equipment {
	private static final String ID = "monksStaff";
	
	public MonksStaff(boolean isUpgraded) {
		super(ID, "Monk's Staff", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE ,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(0, 3, isUpgraded ? 60 : 40, 1, 0.4, DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addReforge(EnduranceTraining.get(), StoneSword.get(), StoneSpear.get(), StoneAxe.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK);
	}
}
