package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.Dexterity;
import me.neoblade298.neorogue.equipment.abilities.Resourcefulness;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ButterflyKnife extends Equipment {
	private static final String ID = "butterflyKnife";
	private static final int base = 25;
	private int dmg, stam;
	public ButterflyKnife(boolean isUpgraded) {
		super(ID, "Butterfly Knife", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 1.25, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		
		dmg = isUpgraded ? 15 : 5;
		stam = isUpgraded ? 35 : 25;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(StoneDriver.get());
		addReforge(Dexterity.get(), EvasiveKnife.get());
		addReforge(Resourcefulness.get(), ButterflyKnife2.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			DamageMeta dm = new DamageMeta(pdata, base + (data.getStamina() >= stam ? dmg : 0), DamageType.SLASHING);
			weaponSwingAndDamage(p, data, ev.getTarget(), dm);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Deal an additional <yellow>" + dmg + "</yellow> damage if above "
				+ "<yellow>" + stam + "</yellow> stamina.");
	}
}
