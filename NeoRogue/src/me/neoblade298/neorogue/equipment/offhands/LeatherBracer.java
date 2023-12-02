package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class LeatherBracer extends Offhand {
	private int instances;
	
	public LeatherBracer(boolean isUpgraded) {
		super("leatherBracer", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Leather Bracer";
		instances = isUpgraded ? 3 : 2;
		item = createItem(this, Material.LEATHER, null, "Prevents the first <yellow>" + instances + "</yellow> instances of taking damage in a fight.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new LeatherBracerInstance(p, this));
	}
	
	private class LeatherBracerInstance extends EquipmentInstance {
		private Player p;
		public LeatherBracerInstance(Player p, Equipment eq) {
			super(eq);
			this.p = p;
		}

		@Override
		public boolean run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ITEM_SHIELD_BREAK, 1F, 1F, false);
			this.setCancelled(true);
			return true; // Only happens once
		}
	}
}
