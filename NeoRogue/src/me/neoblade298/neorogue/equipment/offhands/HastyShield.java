package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HastyShield extends Offhand {
	private int reduction, amount;
	
	public HastyShield(boolean isUpgraded) {
		super("hastyShield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Hasty Shield";
		reduction = 15;
		amount = isUpgraded ? 15 : 10;
		item = createItem(this, Material.SHIELD, null, "When raised, reduce the next hit taken by <yellow>" + reduction + "</yellow>"
				+ " and grant <yellow>" + amount + " </yellow>mana and stamina. 5 second cooldown.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new HastyShieldInstance(this, p));
	}
	
	private class HastyShieldInstance extends EquipmentInstance {
		private Player p;
		private long nextUsable = 0L;
		public HastyShieldInstance(Equipment eq, Player p) {
			super(eq);
			this.p = p;
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
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
}
