package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.GuardianSpirit;
import me.neoblade298.neorogue.equipment.abilities.HerculeanStrength;
import me.neoblade298.neorogue.equipment.abilities.Skirmisher;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class IronSword extends Equipment {
	private static final String ID = "IronSword";
	
	public IronSword(boolean isUpgraded) {
		super(ID, "Iron Sword", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 80 : 65, 1, 0.4, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(GuardianSpirit.get(), Excalibur.get());
		addReforge(HerculeanStrength.get(), SoulHarvester.get());
		addReforge(Skirmisher.get(), HibernianQuickblade.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			if (data.canBasicAttack()) {
				am.addCount(1);
				if (am.getCount() >= 3) {
					am.setCount(0);
					for (EquipmentInstance inst : data.getActiveEquipment().values()) {
						inst.addCooldown(-1);
					}
				}
			}
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "Every <white>third</white> hit lowers ability cooldowns by <white>1</white>.");
	}
}
