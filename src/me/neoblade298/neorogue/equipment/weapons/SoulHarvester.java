package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SoulHarvester extends Equipment {
	private static final String ID = "SoulHarvester";
	private int str;
	
	public SoulHarvester(boolean isUpgraded) {
		super(ID, "Soul Harvester", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(100, 1, 0.4, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		str = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			if (ev.getTarget().getHealth() <= 0) {
				data.applyStatus(StatusType.STRENGTH, data, str, -1);
				Sounds.fire.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Gain " + GlossaryTag.STRENGTH.tag(this, str, true) + " on basic attack kill.");
	}
}
