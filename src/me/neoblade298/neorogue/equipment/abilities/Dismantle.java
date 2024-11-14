package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class Dismantle extends Equipment {
	private static final String ID = "dismantle";
	private int stacks;
	
	public Dismantle(boolean isUpgraded) {
		super(ID, "Dismantle", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stacks = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, new DismantleInstance(data, getUnupgraded(), slot, es));
	}

	private class DismantleInstance extends EquipmentInstance {
		private LivingEntity target;
		public DismantleInstance(PlayerFightData data, Equipment equip, int slot, EquipSlot es) {
			super(data, equip, slot, es);

			action = (pdata, in) -> {
				DealtDamageEvent ev = (DealtDamageEvent) in;
				if (ev.getTarget() == target) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, stacks, -1);
				}
				else {
					target = ev.getTarget();
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_PICKAXE,
				"Passive. Dealing consecutive damage to an enemy applies " + GlossaryTag.INJURY.tag(this, stacks, true) + ".");
	}
}
