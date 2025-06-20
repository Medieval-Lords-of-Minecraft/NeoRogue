package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RingOfScalding extends Equipment {
	private static final String ID = "ringOfScalding";
	private double mult;
	
	public RingOfScalding(boolean isUpgraded) {
		super(ID, "Ring of Scalding", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
				mult = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.FIRE)) {
				double dmg = 0;
				for (DamageSlice slice : ev.getMeta().getSlices()) {
					if (slice.getType() == DamageType.FIRE) {
						dmg += slice.getDamage();
					}
				}
				if (dmg <= 0) return TriggerResult.keep();
				FightInstance.applyStatus(ev.getTarget(), StatusType.BURN, data, (int) (dmg * mult), -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAVA_BUCKET, "Dealing " + GlossaryTag.FIRE.tag(this) + " damage applies " + DescUtil.yellow(mult + "x") + 
			" of the damage dealt as " + GlossaryTag.BURN.tag(this) + ".");
	}
}
