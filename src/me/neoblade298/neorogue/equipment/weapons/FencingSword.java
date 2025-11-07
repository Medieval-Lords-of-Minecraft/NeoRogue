package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.BasicInfusionMastery;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class FencingSword extends Equipment {
	private static final String ID = "fencingSword";
	private int shields;

	public FencingSword(boolean isUpgraded) {
		super(
				ID, "Fencing Sword", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties
						.ofWeapon(isUpgraded ? 40 : 30, 1, 0.3, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		shields = isUpgraded ? 6 : 4;
	}
	
	@Override
	public void setupReforges() {
		addSelfReforge(Rapier.get());
		addReforge(BasicInfusionMastery.get(), CripplingFencingSword.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			if (am.addCount(1) < 3) return TriggerResult.keep();
			am.setCount(0);
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STONE_SWORD,
				"Every <white>3rd</white> hit, grant yourself " + GlossaryTag.SHIELDS.tag(this, shields, true)
						+ " [<white>3s</white>]."
		);
	}
}
