package me.neoblade298.neorogue.equipment.offhands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class MagicRock extends Equipment {
	private static final String ID = "magicRock";
	private int percentDmgBuff;
	
	public MagicRock(boolean isUpgraded) {
		super(ID, "Magic Rock", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
		percentDmgBuff = isUpgraded ? 25 : 15;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger Bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			
			List<DamageSlice> newSlices = new ArrayList<>();
			ev.getMeta().getSlices().forEach(slice -> {
				if (slice.getType().containsBuffType(BuffType.EARTHEN)
						|| slice.getType().containsBuffType(BuffType.BLUNT)) {
					newSlices.add(
							new DamageSlice(
									pdata, (1 + percentDmgBuff / 100.0) * slice.getDamage() / 2, DamageType.EARTHEN
							)
					);
					newSlices.add(
							new DamageSlice(
									pdata, (1 + percentDmgBuff / 100.0) * slice.getDamage() / 2, DamageType.BLUNT
							)
					);
				}
			});

			ev.getMeta().getSlices().removeIf(
					slice -> slice.getType().containsBuffType(BuffType.EARTHEN)
							|| slice.getType().containsBuffType(BuffType.BLUNT)
			);
			
			newSlices.forEach(slice -> ev.getMeta().addDamageSlice(slice));
			
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STONE_BUTTON,
				"All " + GlossaryTag.EARTHEN.tag(this) + " and " + GlossaryTag.BLUNT.tag(this)
						+ " damage dealt gets converted to an equal proportion of both, and then buffed <yellow>"
						+ percentDmgBuff + "%</yellow>."
		);
	}
}
