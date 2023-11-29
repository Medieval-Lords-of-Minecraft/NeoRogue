package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class SpikyShield extends Offhand {
	private int reduction, amount;
	
	public SpikyShield(boolean isUpgraded) {
		super("spikyShield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Spiky Shield";
		reduction = 8;
		amount = isUpgraded ? 6 : 4;
		item = createItem(this, Material.SHIELD, null, "When raised, reduce all damage taken by <yellow>" + reduction + "</yellow>."
				+ " Also grants <yellow>" + amount + "</yellow> thorns at the start of combat.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new HastyShieldInstance(this, p, data));
	}
	
	private class HastyShieldInstance extends EquipmentInstance {
		private Player p;
		private PlayerFightData data;
		private long nextUsable = 0L;
		public HastyShieldInstance(Equipment eq, Player p, PlayerFightData data) {
			super(eq);
			this.p = p;
			this.data = data;
		}
		
		@Override
		public boolean run(Object[] inputs) {
			if (!p.isHandRaised()) return false;
			long now = System.currentTimeMillis();
			if (now <= nextUsable) return false;
			
			nextUsable = now + 5000L; // 5s
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			data.addMana(amount);
			data.addStamina(amount);
			return false;
		}
	}
}
