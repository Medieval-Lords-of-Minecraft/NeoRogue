package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HastyShield extends Equipment {
	private int reduction, amount;
	
	public HastyShield(boolean isUpgraded) {
		super("hastyShield", "Hasty Shield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = 15;
		amount = isUpgraded ? 15 : 10;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new HastyShieldInstance(p));
	}
	
	private class HastyShieldInstance implements TriggerAction {
		private Player p;
		private long nextUsable = 0L;
		public HastyShieldInstance(Player p) {
			this.p = p;
		}
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			if (!p.isHandRaised()) return TriggerResult.keep();
			long now = System.currentTimeMillis();
			if (now <= nextUsable) return TriggerResult.keep();
			
			nextUsable = now + 5000L; // 5s
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			data.addMana(amount);
			data.addStamina(amount);
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduce the next hit taken by <yellow>" + reduction + "</yellow>"
				+ " and grant <yellow>" + amount + " </yellow>mana and stamina. 5 second cooldown.");
	}
}
