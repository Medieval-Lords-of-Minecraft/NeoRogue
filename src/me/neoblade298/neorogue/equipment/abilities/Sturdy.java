package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Sturdy extends Equipment {
	private static final String ID = "Sturdy";
	private int shields, cd;
	
	public Sturdy(boolean isUpgraded) {
		super(ID, "Sturdy", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		cd = (int) properties.get(PropertyType.COOLDOWN);
		shields = isUpgraded ? 12 : 8;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(GraniteShield.get(), Bulwark.get(), Endurance.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		SturdyInstance inst = new SturdyInstance(id, p);
		data.addTrigger(id, Trigger.RAISE_SHIELD, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			if (inst.s != null) inst.s.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GREEN_DYE,
				"Passive. Raising a shield grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " until "
				+ "you lower your shield again.");
	}
	
	private class SturdyInstance extends PriorityAction {
		private Shield s;
		private long nextUse;
		public SturdyInstance(String id, Player p) {
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
