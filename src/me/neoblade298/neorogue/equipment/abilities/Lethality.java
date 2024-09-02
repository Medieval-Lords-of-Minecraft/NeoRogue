package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class Lethality extends Equipment {
	private static final String ID = "lethality";
	private int thres, inc;
	
	public Lethality(boolean isUpgraded) {
		super(ID, "Lethality", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 30 : 40;
		inc = isUpgraded ? 40 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (data.getStamina() < thres) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			ev.getMeta().addBuff(BuffType.PHYSICAL, new Buff(data, inc, 0), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				"Passive. Increase " + GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + inc + "</yellow>"
				+ " while above <yellow>" + thres + "</yellow> stamina.");
	}
}
