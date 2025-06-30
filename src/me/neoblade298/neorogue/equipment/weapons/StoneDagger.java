package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneDagger extends Equipment {
	private static final String ID = "stoneDagger";
	private int damage;
	
	public StoneDagger(boolean isUpgraded) {
		super(ID, "Stone Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 30 : 25, 1.5, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
		damage = isUpgraded ? 10 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			DamageMeta dm = new DamageMeta(data, this, true, DamageStatTracker.of(id + slot, this));
			if (data.hasStatus(StatusType.STEALTH)) dm.addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, this)));
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwing(p, data);
			FightInstance.dealDamage(dm, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Deal an additional " + GlossaryTag.DARK.tag(this, damage, true) + " when in "
				+ GlossaryTag.STEALTH.tag(this) + ".");
	}
}
