package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Enlighten extends Equipment {
	private static final String ID = "Enlighten";
	private int sanct;
	
	public Enlighten(boolean isUpgraded) {
		super(ID, "Enlighten", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 0, 0));
		
		sanct = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			ActionMeta am = new ActionMeta();
			data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata2, in2) -> {
				am.setBool(true);
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata3, in3) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in3;
				if (am.getBool()) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.SANCTIFIED, data, sanct, -1);
					am.setBool(false);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.POWER.tag(this) + ". Every time you apply " + GlossaryTag.SHIELDS.tag(this) + ", your next basic attack applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ". Does not stack.");
	}
}
