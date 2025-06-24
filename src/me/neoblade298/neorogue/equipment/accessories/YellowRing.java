package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class YellowRing extends Equipment {
	private static final String ID = "yellowRing";
	private int elec;
	public YellowRing(boolean isUpgraded) {
		super(ID, "Yellow Ring", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		elec = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		double max = data.getMaxMana() * 0.5;
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (data.getMana() <= max) {
				return TriggerResult.keep();
			}
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.LIGHTNING)) {
				FightData fd = FightInstance.getFightData(ev.getTarget());
				fd.applyStatus(StatusType.ELECTRIFIED, data, elec, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.YELLOW_DYE, "Dealing " + GlossaryTag.LIGHTNING.tag(this) + " damage while above <white>50%</white> mana also applies " +
		GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
