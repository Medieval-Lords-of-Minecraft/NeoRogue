package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class DropOfInsanity extends Consumable {
	private static final String ID = "DropOfInsanity";

	public DropOfInsanity(boolean isUpgraded) {
		super(ID, "Drop of Insanity", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		ActionMeta selected = new ActionMeta();

		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in;

			Object obj = selected.getObject();
			if (obj == null) {
				// First cast: select this ability if it qualifies
				EquipmentInstance inst = ev.getInstance();
				CastType type = inst.getEquipment().getProperties().getCastType();
				if (inst.getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
				if (type != CastType.STANDARD && type != CastType.POST_TRIGGER) return TriggerResult.keep();
				selected.setObject(inst);
			} else {
				EquipmentInstance selectedInst = (EquipmentInstance) obj;
				if (selectedInst != ev.getInstance()) return TriggerResult.keep();
			}

			ev.addBuff(PropertyType.MANA_COST, id,
					Buff.multiplier(data, 1, BuffStatTracker.of(id, this, PropertyType.MANA_COST.getDisplay() + " reduced")));
			ev.addBuff(PropertyType.STAMINA_COST, id,
					Buff.multiplier(data, 1, BuffStatTracker.of(id, this, PropertyType.STAMINA_COST.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});

		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Your next casted ability becomes free to cast for the rest of the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(72, 0, 102));
		item.setItemMeta(meta);
	}
}