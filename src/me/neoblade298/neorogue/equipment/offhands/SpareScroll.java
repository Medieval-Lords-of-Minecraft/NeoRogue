package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SpareScroll extends Equipment {
	private static final String ID = "spareScroll";
	
	public SpareScroll(boolean isUpgraded) {
		super(ID, "Spare Scroll", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger Bind, EquipSlot es, int slot) {
		SpareScrollInstance inst = new SpareScrollInstance();
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, inst);
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> inst.checkUsed(p, (CastUsableEvent) in));
	}

	public class SpareScrollInstance implements TriggerAction {
		private boolean used = false;
		private String uuid = UUID.randomUUID().toString();
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (!used) {
				CastUsableEvent ev = (CastUsableEvent) inputs;
				if (ev.getInstance().getEffectiveStaminaCost() == 0 && ev.getInstance().getEffectiveManaCost() == 0)
					return TriggerResult.keep();
				if (ev.getBuff(PropertyType.STAMINA_COST).apply(ev.getInstance().getStaminaCost()) <= 0)
					return TriggerResult.keep();

				ev.addBuff(PropertyType.MANA_COST, data, uuid, 1, true);
				ev.addBuff(PropertyType.STAMINA_COST, data, uuid, 1, true);
				if (isUpgraded)
					ev.addBuff(PropertyType.COOLDOWN, data, uuid, 0.5, true);

				return TriggerResult.keep();
			}
			return TriggerResult.remove();
		}

		private TriggerResult checkUsed(Player p, CastUsableEvent ev) {
			if (ev.hasId(uuid)) {
				Sounds.turnPage.play(p, p);
				Util.msg(p, display.append(Component.text(" was activated", NamedTextColor.GRAY)));
				used = true;
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.MOJANG_BANNER_PATTERN,
				isUpgraded ? "The first skill you cast is free."
						: "The first skill you cast is free and goes on half the cooldown."
		);
	}
}