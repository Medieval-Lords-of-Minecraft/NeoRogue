package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneDagger extends Equipment {
	private static final String ID = "stoneDagger";
	private static int damage, base = 15;
	
	public StoneDagger(boolean isUpgraded) {
		super(ID, "Stone Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 1.5, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damage = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			DamageMeta dm = new DamageMeta(data, base, DamageType.SLASHING);
			if (data.hasStatus(StatusType.INVISIBLE)) dm.addDamageSlice(new DamageSlice(data, damage, DamageType.DARK));
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget(), dm);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Deal an additional " + GlossaryTag.DARK.tag(this, damage, true) + " when invisible.");
	}
}
