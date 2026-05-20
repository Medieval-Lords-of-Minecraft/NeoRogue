package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Endurance extends Equipment {
	private static final String ID = "Endurance";
	private int shields, berserk, cd;
	
	public Endurance(boolean isUpgraded) {
		super(ID, "Endurance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		shields = 15;
		berserk = isUpgraded ? 3 : 2;
		cd = (int) properties.get(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		EndureInstance inst = new EndureInstance(id, p);
		data.addTrigger(id, Trigger.RAISE_SHIELD, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			if (inst.s != null) inst.s.remove();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			data.applyStatus(StatusType.BERSERK, data, berserk, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COAL,
				"Passive. Raising a shield grants " + GlossaryTag.SHIELDS.tag(this, shields, false) + " until "
				+ "you lower your shield again. Give yourself " +
						GlossaryTag.BERSERK.tag(this, berserk, true) + " anytime an enemy hits you with your shield raised.");
	}
	
	private class EndureInstance extends PriorityAction {
		private Shield s;
		private long nextUse;
		public EndureInstance(String id, Player p) {
			super(id);
			action = (pdata, inputs) -> {
				if (System.currentTimeMillis() < nextUse) return TriggerResult.keep();
				s = pdata.addPermanentShield(p.getUniqueId(), shields);
				nextUse = System.currentTimeMillis() + (cd * 1000);
				return TriggerResult.keep();
			};
		}
	}
}
