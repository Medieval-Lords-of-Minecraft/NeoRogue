package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.DescUtil;
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

public class Bulwark extends Equipment {
	private static final String ID = "Bulwark";
	private int shields, prot, cd;
	
	public Bulwark(boolean isUpgraded) {
		super(ID, "Bulwark", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 5, 0));
		shields = 15;
		prot = isUpgraded ? 3 : 2;
		cd = (int) properties.get(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		BulwarkInstance inst = new BulwarkInstance(id, data);
		data.addTrigger(id, Trigger.RAISE_SHIELD, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			if (inst.s != null) inst.s.remove();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, in) -> {
			Player p = data.getPlayer();
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			data.applyStatus(StatusType.PROTECT, data, prot, 60);
			data.applyStatus(StatusType.SHELL, data, prot, 60);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + ". Raising a shield grants " + GlossaryTag.SHIELDS.tag(this, shields, false) + " until "
				+ "you lower your shield again. For every second the shield remains raised, grant " + GlossaryTag.PROTECT.tag(this, prot, true) +
				" and " + GlossaryTag.SHELL.tag(this, prot, true) + " " + DescUtil.duration(3, false) + ".");
	}
	
	private class BulwarkInstance extends PriorityAction {
		private Shield s;
		private long nextUse;
		public BulwarkInstance(String id, PlayerFightData data) {
			super(id);
			action = (pdata, inputs) -> {
				if (System.currentTimeMillis() < nextUse) return TriggerResult.keep();
				s = pdata.addPermanentShield(data.getPlayer().getUniqueId(), shields);
				nextUse = System.currentTimeMillis() + (cd * 1000);
				return TriggerResult.keep();
			};
		}
	}
}
