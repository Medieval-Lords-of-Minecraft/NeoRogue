package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class Frenzy extends Equipment {
	private static final String ID = "frenzy";
	private int atkSpeed;
	private static final int CUTOFF = 5;
	
	public Frenzy(boolean isUpgraded) {
		super(ID, "Frenzy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		atkSpeed = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.WEAPON_SWING, (pdata, in) -> {
			int mult = Math.min(4, data.getStatus(StatusType.BERSERK).getStacks() / CUTOFF);
			WeaponSwingEvent ev = (WeaponSwingEvent) in;
			ev.getAttackSpeedBuffList().add(new Buff(data, 0, mult * atkSpeed * 0.01));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW,
				"Passive. For every " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + " you have, up to <white>20</white>, increase your attack speed by"
				+ " <yellow>" + atkSpeed + "%</yellow>.");
	}
}
