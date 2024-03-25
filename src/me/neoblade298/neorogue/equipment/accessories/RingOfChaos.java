package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RingOfChaos extends Equipment {
	private static final String ID = "ringOfChaos";
	private int buffPercent;
	private DamageType lastDamageType;
	
	public RingOfChaos(boolean isUpgraded) {
		super(ID, "Ring Of Chaos", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		buffPercent = isUpgraded ? 25 : 12;
		lastDamageType = null;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;

			DamageType primaryType = null;
			double primaryDmg = 0;
			for (DamageSlice slice : ev.getMeta().getSlices()) {
				if (slice.getDamage() > primaryDmg) {
					primaryType = slice.getPostBuffType();
					primaryDmg = slice.getDamage();
				}
			}
			
			if (lastDamageType == null || primaryType != lastDamageType) {
				ev.getMeta().addBuff(BuffType.MAGICAL, new Buff(data, 0, buffPercent / 100.0), BuffOrigin.NORMAL, true);
			}

			lastDamageType = primaryType;
			
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.GOLD_NUGGET,
				"Whenever you deal magic damage, if it's different than the primary type used last, buff this damage by <yellow>"
						+ buffPercent + "%</yellow>."
		);
	}
}
