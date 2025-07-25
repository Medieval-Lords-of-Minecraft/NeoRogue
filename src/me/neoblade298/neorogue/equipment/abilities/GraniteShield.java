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
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class GraniteShield extends Equipment {
	private static final String ID = "graniteShield";
	private int shields, cd, conc;
	
	public GraniteShield(boolean isUpgraded) {
		super(ID, "Granite Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		
		shields = 15;
		cd = (int) properties.get(PropertyType.COOLDOWN);
		conc = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		GraniteShieldInstance inst = new GraniteShieldInstance(id, p);
		data.addTrigger(id, Trigger.RAISE_SHIELD, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			if (inst.s != null) inst.s.remove();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getDamager().applyStatus(StatusType.CONCUSSED, data, conc, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRANITE_SLAB,
				"Passive. Raising a shield grants " + GlossaryTag.SHIELDS.tag(this, shields, false) + " until "
				+ "you lower your shield again. Apply " +
						GlossaryTag.CONCUSSED.tag(this, conc, true) + " to enemies that damage you while your shield is raised.");
	}
	
	private class GraniteShieldInstance extends PriorityAction {
		private Shield s;
		private long nextUse;
		public GraniteShieldInstance(String id, Player p) {
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
