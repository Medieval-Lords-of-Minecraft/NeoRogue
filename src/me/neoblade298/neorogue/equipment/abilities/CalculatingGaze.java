package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class CalculatingGaze extends Equipment {
	private static final String ID = "calculatingGaze";
	private int shields, def;
	private static final int THRES = 30;
	
	public CalculatingGaze(boolean isUpgraded) {
		super(ID, "Calculating Gaze", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
				shields = 5;
				def = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			if (data.getMana() > THRES) {
				ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Passive. Gain " + GlossaryTag.SHIELDS.tag(this, shields, false) + " at the start of a fight. " +
				"Increase defense by " + DescUtil.yellow(def) + " when above " + DescUtil.white(THRES) + " mana.");
	}
}
