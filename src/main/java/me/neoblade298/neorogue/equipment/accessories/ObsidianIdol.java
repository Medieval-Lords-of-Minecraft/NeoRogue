package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ObsidianIdol extends Equipment {
	private static final String ID = "ObsidianIdol";
	private int damagePerStack;

	public ObsidianIdol(boolean isUpgraded) {
		super(ID, "Obsidian Idol", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.ACCESSORY);
		damagePerStack = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			int damage = damagePerStack * ev.getStacks();
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, this)), ev.getTarget().getEntity());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN, "Applying " + GlossaryTag.CONCUSSED.tag(this) + " also deals "
				+ GlossaryTag.EARTHEN.tag(this, damagePerStack, true) + " damage for each stack applied.");
	}
}
