package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ShadowyDagger extends Equipment {
	private static final String ID = "ShadowyDagger";
	private static int base = 40;
	private int dmg;
	
	public ShadowyDagger(boolean isUpgraded) {
		super(ID, "Shadowy Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 1.25, 0.2, DamageType.DARK, Sounds.fire));
		dmg = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Player p = data.getPlayer();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			boolean hasStatus = FightInstance.getFightData(ev.getTarget()).hasStatus(StatusType.INSANITY);
			weaponSwingAndDamage(p, data, ev.getTarget(), base + (hasStatus ? dmg : 0));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD, "Deals an additional <yellow>" + dmg + "</yellow> damage to enemies with " + GlossaryTag.INSANITY.tag(this) + ".");
	}
}
